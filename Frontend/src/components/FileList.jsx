import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { fetchFiles } from "../store/slices/filesSlice";
import {deleteByDocumentId} from "../store/slices/filesSlice"
import { useAuth } from "react-oidc-context";
import axios from "axios";
import "../styles/FileList.css";

export default function FileList() {
  const dispatch = useDispatch();
  const auth = useAuth();
  const filesState = useSelector((s) => s.files);

  useEffect(() => {
    if (auth.isAuthenticated) {
      dispatch(fetchFiles({ token: auth.user?.id_token }));
    }
  }, [auth.isAuthenticated, auth.user, dispatch]);

  const handleDelete = async (documentId) => {
    if (!window.confirm("Are you sure you want to delete this file?")) return;
    // dispatch(
    //   deleteFile({
    //     documentId,
    //     token: auth.user?.id_token,
    //   })
    // );
    try {
      const response = await axios.delete(
        `http://localhost:8081/api/documents/${documentId}`,
        {
          headers: { Authorization: `Bearer ${auth.user?.id_token}` },
        }
      ).then(()=>dispatch(deleteByDocumentId(documentId)));
    } catch (err) {
      alert("Delete failed!");
    }
  };

  const downloadFile = async (documentId) => {
    try {
      const response = await axios.get(
        `http://localhost:8081/api/documents/download/${documentId}`,
        {
          headers: { Authorization: `Bearer ${auth.user?.id_token}` },
        }
      );
      window.open(response.data, "_blank");
    } catch (err) {
      console.error("Download failed", err);
      alert("Download failed!");
    }
  };

  if (filesState.loading)
    return <div className="filelist-loading">Loading files...</div>;

  if (filesState.error)
    return <div className="filelist-error">Error: {filesState.error}</div>;

  return (
    <div className="filelist-wrapper">
      <ul className="filelist">
        {filesState.items.length === 0 && (
          <li className="filelist-empty">No files yet</li>
        )}

        {filesState.items.map((f) => (
          <li key={f.documentId} className="file-item-row">
            <div className="file-info">
              <div className="file-name">{f.fileName}</div>
              <div className="file-meta">
                {f.fileType} • {Math.round(f.fileSize / 1024)} KB
              </div>
            </div>

            <div className="file-status-wrapper">
              <span className={`file-status ${f.status.toLowerCase()}`}>
                {f.status}
              </span>
              {f.status === "COMPLETED" && (
                <button
                  className="px-3 py-1 bg-blue-500 text-white rounded hover:bg-blue-600"
                  onClick={() => downloadFile(f.documentId)}
                >
                  Download
                </button>
              )}
              <button
                className="px-3 py-1 bg-red-500 text-white rounded hover:bg-red-600 ml-2"
                onClick={() => handleDelete(f.documentId)}
              >
                Delete
              </button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}
