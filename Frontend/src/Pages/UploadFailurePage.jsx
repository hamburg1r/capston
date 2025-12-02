import React from "react";
import { useLocation, Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import "../styles/UploadFailurePage.css";

export default function UploadFailurePage() {
  const { state } = useLocation();
  const reason = state?.reason || "Unknown error";

  return (
    <>
      <Navbar />
    <div className="failure-page">
      <div className="failure-card">
        <div className="failure-icon">⚠️</div>
        <h2 className="failure-title">Upload Failed</h2>
        <p className="failure-reason">Reason: {reason}</p>
        <Link to="/upload" className="retry-btn">
          Try Again
        </Link>
      </div>
    </div>
    </>
  );
}
