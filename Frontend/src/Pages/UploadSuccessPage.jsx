import React from "react";
import { useLocation, Link } from "react-router-dom";
import Navbar from "../components/Navbar";

export default function UploadSuccessPage() {
  const { state } = useLocation();
  const documentId = state?.documentId;

  return (
    <div>
      <Navbar />
      <div className="max-w-md mx-auto mt-20 p-6 bg-white rounded shadow text-center">
        <h2 className="text-2xl font-semibold mb-2">Upload Successful</h2>
        <p className="text-gray-600">Document ID: {documentId}</p>
        <Link to="/files" className="mt-6 inline-block bg-blue-600 text-white px-4 py-2 rounded">View Files</Link>
      </div>
    </div>
  );
}
