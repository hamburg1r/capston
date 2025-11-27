import React from "react";
import { useLocation, Link } from "react-router-dom";
import Navbar from "../components/Navbar";

export default function UploadFailurePage() {
  const { state } = useLocation();
  const reason = state?.reason || "Unknown error";

  return (
    <div>
      <Navbar />
      <div className="max-w-md mx-auto mt-20 p-6 bg-white rounded shadow text-center">
        <h2 className="text-2xl font-semibold mb-2 text-red-600">Upload Failed</h2>
        <p className="text-gray-600">Reason: {reason}</p>
        <Link to="/upload" className="mt-6 inline-block bg-gray-800 text-white px-4 py-2 rounded">Try Again</Link>
      </div>
    </div>
  );
}
