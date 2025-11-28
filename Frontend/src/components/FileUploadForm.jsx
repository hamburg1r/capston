import React, { useRef, useState, useContext } from "react";
import { useAuth } from "react-oidc-context";
import { useDispatch } from "react-redux";
import imageCompression from "browser-image-compression";
import axiosClient from "../api/axiosClient";
import {
  addFileOptimistic,
  updateFileStatus,
} from "../store/slices/filesSlice";
import { AppConfigContext } from "../context/AppConfigContext";
import { useNavigate } from "react-router-dom";
import "../styles/FileUploadForm.css";

export default function FileUploadForm() {
  const fileRef = useRef();
  const auth = useAuth();
  const dispatch = useDispatch();
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const appConfig = useContext(AppConfigContext);
  const navigate = useNavigate();
  const [isDragOver, setIsDragOver] = useState(false);

  const handleFile = async (file) => {
    if (!file) return;

    try {
      setUploading(true);

      // optional image compression
      let fileToUpload = file;
      if (file.type.startsWith("image/")) {
        try {
          fileToUpload = await imageCompression(file, {
            maxSizeMB: 1,
            maxWidthOrHeight: 1920,
            useWebWorker: true,
          });
        } catch {
          fileToUpload = file;
        }
      }

      // 1. Request presigned URL
      const token = auth.user?.id_token;
      const presignRes = await axiosClient.post(
        "/api/documents/presigned-url",
        { fileName: file.name, fileType: file.type, fileSize: file.size },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      const { uploadUrl, documentId } = presignRes.data;

      // Optimistic update
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

      // 2. Upload to S3
      const putResp = await fetch(uploadUrl, {
        method: "PUT",
        body: fileToUpload,
        headers: { "Content-Type": fileToUpload.type },
      });

      if (!putResp.ok) {
        dispatch(updateFileStatus({ documentId, status: "FAILED" }));
        navigate("/upload-failure", {
          state: { documentId, reason: "S3 upload failed" },
        });
        return;
      }

      // 3. Mark complete in backend
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
    const file = e.target.files[0];
    await handleFile(file);
  };

  const handleDrop = async (e) => {
    e.preventDefault();
    setIsDragOver(false);

    const file = e.dataTransfer.files[0];
    if (file) await handleFile(file);
  };
  return (
    <div className="upload-form">
      <h3 className="upload-title">Upload File</h3>

      <input
        type="file"
        ref={fileRef}
        onChange={onChange}
        className="file-input hidden"
        id="file-upload-input"
      />

          {/* Dropzone */}
      <div
        onClick={() => fileRef.current?.click()}
        onDragOver={(e) => {
          e.preventDefault();
          setIsDragOver(true);
        }}
        onDragLeave={() => setIsDragOver(false)}
        onDrop={handleDrop}
        className={`
          flex flex-col items-center justify-center
          w-full h-40 cursor-pointer transition
          border-2 border-dashed rounded-xl
          ${isDragOver ? "border-blue-500 bg-blue-50" : "border-gray-400 bg-gray-50"}
        `}
      >
        <span className="font-semibold text-gray-700">Drop file here</span>
        <span className="text-sm text-gray-500">or click to upload</span>
      </div>



      {uploading && (
        <div className="progress-section">
          <p className="progress-text">Uploading... {progress}%</p>
          <progress
            value={progress}
            max="100"
            className="progress-bar"
          ></progress>
        </div>
      )}

      <p className="upload-note">
        Files are uploaded directly to AWS S3 via presigned URL.
      </p>
    </div>
  );
}
