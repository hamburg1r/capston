import React from "react";
import { useNavigate } from "react-router-dom";
import "../styles/FileItem.css";

export default function FileItem({ file }) {
  const navigate = useNavigate();

  const openPreview = () => {
    navigate(`/files/${file.id}`);
  };

  return (
    <div className="file-item">
      <div className="file-info">
        <div className="file-name">{file.name}</div>

        <div className="file-meta">
          <span className="meta-label">Type:</span> {file.mimeType}
        </div>

        <div className="file-meta">
          <span className="meta-label">Size:</span> {file.size} KB
        </div>
      </div>

      <div className="file-actions">
        <button className="btn-preview" onClick={openPreview}>
          Preview
        </button>

        <a href={file.url} download={file.name} className="download-link">
          <button className="btn-download">Download</button>
        </a>
      </div>
    </div>
  );
}
