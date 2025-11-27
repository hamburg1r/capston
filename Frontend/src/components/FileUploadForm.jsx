import React, { useRef, useState, useContext } from "react";
import { useAuth } from "react-oidc-context";
import { useDispatch } from "react-redux";
import imageCompression from "browser-image-compression";
import axiosClient from "../api/axiosClient";
import { requestPresigned, addFileOptimistic, updateFileStatus } from "../store/slices/filesSlice";
import { AppConfigContext } from "../context/AppConfigContext";
import { useNavigate } from "react-router-dom";
/**
 * Flow:
 * 1. User picks file
 * 2. Optionally compress if image
 * 3. POST to backend /presigned-url with file name/type + Authorization header
 * 4. Backend returns { uploadUrl, documentId, s3Key }
 * 5. PUT file to uploadUrl (no auth header)
 * 6. POST /api/documents/{documentId}/complete (with Authorization) to mark upload complete
 * 7. Navigate to success or failure pages
 */
export default function FileUploadForm() {
  const fileRef = useRef();
  const auth = useAuth();
  const dispatch = useDispatch();
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const appConfig = useContext(AppConfigContext);
  const navigate = useNavigate();
  const handleFile = async (file) => {
    if (!file) return;
    try {
      setUploading(true);
      // optional image compression for images
      let fileToUpload = file;
      if (file.type.startsWith("image/")) {
        const opts = { maxSizeMB: 1, maxWidthOrHeight: 1920, useWebWorker: true };
        try {
          fileToUpload = await imageCompression(file, opts);
        } catch (e) {
          // compression failed; continue with original
          fileToUpload = file;
        }
      }
      // 1. Request presigned URL from backend
      const token = auth.user?.id_token;
      const presignRes = await axiosClient.post(
        "/api/documents/presigned-url",
        { fileName: file.name, fileType: file.type, fileSize : file.size },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      const { uploadUrl, documentId, s3Key } = presignRes.data;
      // Optimistic local add
      dispatch(
        addFileOptimistic({
          documentId,
          fileName: file.name,
          fileType: file.type,
          fileSize: file.size,
          status: "UPLOADING",
          uploadDate: new Date().toISOString(),
        })
      );
      // 2. PUT to S3 (use native fetch to stream and track)
      const putResp = await fetch(uploadUrl, {
        method: "PUT",
        body: fileToUpload,
        headers: {
          "Content-Type": fileToUpload.type,
        },
      });
      if (!putResp.ok) {
        // PUT failed
        dispatch(updateFileStatus({ documentId, status: "FAILED" }));
        navigate("/upload-failure", { state: { documentId, reason: "S3 upload failed" } });
        return;
      }
      // 3. Notify backend upload complete
      const completeResp = await axiosClient.post(
        `/api/documents/${documentId}/complete`,
        {
           fileName: file.name,
          fileType: file.type,
          fileSize: file.size,
        },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      if (completeResp.status === 200 || completeResp.status === 204) {
        dispatch(updateFileStatus({ documentId, status: "COMPLETED" }));
        navigate("/upload-success", { state: { documentId } });
      } else {
        dispatch(updateFileStatus({ documentId, status: "PROCESSING" }));
        navigate("/upload-success", { state: { documentId } });
      }
    } catch (err) {
      console.error("Upload error", err);
      navigate("/upload-failure", { state: { reason: err.message } });
    } finally {
      setUploading(false);
      setProgress(0);
      if (fileRef.current) fileRef.current.value = "";
    }
  };
  const onChange = async (e) => {
    const f = e.target.files[0];
    await handleFile(f);
  };
  return (
    <div className="p-6 bg-white rounded shadow max-w-2xl mx-auto">
      <h3 className="text-xl font-semibold mb-4">Upload File</h3>
      <input type="file" ref={fileRef} onChange={onChange} className="mb-4" />
      {uploading && (
        <div>
          <p>Uploading... {progress}%</p>
          <progress value={progress} max="100" className="w-full"></progress>
        </div>
      )}
      <p className="mt-3 text-sm text-gray-500">Files are uploaded directly to S3 via presigned URL.</p>
    </div>
  );
}