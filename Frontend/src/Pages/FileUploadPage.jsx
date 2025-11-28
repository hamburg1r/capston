import React from "react";
import Navbar from "../components/Navbar";
import ProtectedRoute from "../components/ProtectedRoute";
import FileUploadForm from "../components/FileUploadForm";
import "../styles/FileUploadPage.css";

export default function FileUploadPage() {
  return (
    <ProtectedRoute>
      <div className="upload-page">
        <Navbar />
        <div className="upload-container">
          <FileUploadForm />
        </div>
      </div>
    </ProtectedRoute>
  );
}
