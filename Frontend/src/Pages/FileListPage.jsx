import React from "react";
import Navbar from "../components/Navbar";
import ProtectedRoute from "../components/ProtectedRoute";
import FileList from "../components/FileList";

export default function FileListPage() {
  return (
    <ProtectedRoute>
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="max-w-4xl mx-auto py-12">
          <h2 className="text-2xl mb-4">Your Files</h2>
          <FileList />
        </div>
      </div>
    </ProtectedRoute>
  );
}
