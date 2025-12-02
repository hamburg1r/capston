import axios from "axios";
import React from "react";
import { useAuth } from "react-oidc-context";
import { Link } from "react-router-dom";

function FileItem({ file }) {
const auth = useAuth();

console.log(file.documentId)

   const downloadFile = async (documentId) => {
    try {
      const response = await axios.get(
        `http://localhost:8081/api/documents/download/${documentId}`,
        {
          headers: { Authorization: `Bearer ${auth.user?.id_token}` },
        }
      );
      
      window.open(response.data.downloadUrl, "_blank");
    } catch (err) {
      console.error("Download failed", err);
      alert("Download failed!");
    }
  };
  if (!file) return null;

  return (
    <div
      style={{
        padding: "20px",
        borderRadius: "10px",
        border: "1px solid #ddd",
        width: "500px",
        margin: "20px auto",
        boxShadow: "0 4px 10px rgba(0,0,0,0.05)",
        fontFamily: "Arial",
      }}
    >

      <div style={{display:'flex', justifyContent:'space-between'}}>
        
      <h2 style={{ marginBottom: "10px", color: "#333" }}>📄 File Details</h2>
      <Link to={'..'}>
      <button className="px-3 py-1 bg-blue-500 text-white rounded hover:bg-blue-600">
             Go Back
      </button>
      </Link>
      </div>

      <div style={{ marginBottom: "10px" }}>
        <strong>Document ID:</strong>
        <div>{file.documentId}</div>
      </div>

      <div style={{ marginBottom: "10px" }}>
        <strong>File Name:</strong>
        <div>{file.fileName}</div>
      </div>

      <div style={{ marginBottom: "10px" }}>
        <strong>File Type:</strong>
        <div>{file.fileType}</div>
      </div>

      <div style={{ marginBottom: "10px" }}>
        <strong>File Size:</strong>
        <div>{(file.fileSize / (1024 * 1024)).toFixed(2)} MB</div>
      </div>

      <div style={{ marginBottom: "10px" }}>
        <strong>Status:</strong>
        <div
          style={{
            color: file.status === "COMPLETED" ? "green" : "orange",
            fontWeight: "bold",
          }}
        >
          {file.status}
        </div>
      </div>

      <div style={{ marginBottom: "10px" }}>
        <strong>Upload Date:</strong>
        <div>{new Date(file.uploadDate).toLocaleString()}</div>
      </div>

      <div style={{ marginBottom: "10px" }}>
        <strong>User ID:</strong>
        <div>{file.userId}</div>
      </div>

      <div style={{ marginBottom: "10px" }}>
        <strong>S3 Key:</strong>
        <div style={{ fontSize: "13px", color: "#555" }}>{file.s3Key}</div>
      </div>

      <button
        style={{
          marginTop: "15px",
          padding: "10px 20px",
          backgroundColor: "#007bff",
          color: "white",
          border: "none",
          borderRadius: "5px",
          cursor: "pointer",
        }}
        onClick={() =>downloadFile(file.documentId)}
      >
        ⬇️ Download File
      </button>
    </div>
  );
}

export default FileItem;
