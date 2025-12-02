import axios from "axios";
import React from "react";
import { useAuth } from "react-oidc-context";
import { Link } from "react-router-dom";

function FileItem({ file }) {
const auth = useAuth();

// console.log(file.documentId)

  const downloadDirect = async (documentId, fileName) => {
  try {
    const response = await fetch(
      `http://localhost:8081/api/documents/download-direct/${documentId}`,
      {
        method: "GET",
        headers: {
          Authorization: `Bearer ${auth.user?.id_token}`,
        },
      }
    );

    console.log(response)
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = fileName; // auto file save
    link.click();
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error("Download failed:", error);
    alert("Failed to download!");
  }
};



const viewFile = async (documentId) => {
    try {
      const response = await axios.get(
        `http://localhost:8081/api/documents/view/${documentId}`,
        {
          headers: { Authorization: `Bearer ${auth.user?.id_token}` },
        }
      );
      window.open(response.data.viewUrl, "_blank");
    } catch (err) {
      console.error("View Request failed", err);
      alert("View Request failed!");
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
        onClick={() =>downloadDirect(file.documentId, file.fileName)}
      >
        ⬇️ Download File
      </button>
      <button
        style={{
          marginTop: "15px",
          padding: "10px 20px",
          backgroundColor: "#007bff",
          color: "white",
          border: "none",
          borderRadius: "5px",
          cursor: "pointer",
          marginLeft:"10px"
        }}
        onClick={() =>viewFile(file.documentId)}
      >
        View
      </button>
    </div>
  );
}

export default FileItem;
