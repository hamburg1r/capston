import React from "react";
import Navbar from "../components/Navbar";
import ProtectedRoute from "../components/ProtectedRoute";
import FileUploadForm from "../components/FileUploadForm";

export default function FileUploadPage() {
  return (
    <ProtectedRoute>
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="max-w-3xl mx-auto py-12">
          <FileUploadForm />
        </div>
      </div>
    </ProtectedRoute>
  );
}
