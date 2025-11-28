import React from "react";
import { useLocation, Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import "../styles/UploadSuccessPage.css";

export default function UploadSuccessPage() {
  const { state } = useLocation();
  const documentId = state?.documentId;

  return (
    <div className="success-page">
      <Navbar />

      <div className="success-card">
        <div className="success-icon">✅</div>

        <h2 className="success-title">Upload Successful</h2>

        <p className="success-info">
          Document ID: <span className="doc-id">{documentId}</span>
        </p>

        <Link to="/files" className="success-btn">
          View Your Files
        </Link>
      </div>
    </div>
  );
}
