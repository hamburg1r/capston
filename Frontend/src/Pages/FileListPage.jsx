import React from "react";
import Navbar from "../components/Navbar";
import ProtectedRoute from "../components/ProtectedRoute";
import FileList from "../components/FileList";
import "../styles/FileListPage.css";

export default function FileListPage() {
  return (
    <ProtectedRoute>
      <div className="filelist-page">
        <Navbar />

        <div className="filelist-container">
          <h2 className="filelist-title">Your Files</h2>
          <FileList />
        </div>
      </div>
    </ProtectedRoute>
  );
}
