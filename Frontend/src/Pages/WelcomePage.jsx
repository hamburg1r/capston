import React from "react";
import Navbar from "../components/Navbar";
import { useSelector } from "react-redux";
import { Link } from "react-router-dom";

export default function WelcomePage() {
  const profile = useSelector((s) => s.auth.profile);
   const authState = useSelector((state) => state.auth);
   console.log("🔍 Redux Auth State:", authState);


  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-4xl mx-auto py-12">
        <h2 className="text-3xl font-semibold">Welcome, {profile?.email || profile?.name}</h2>
        <p className="mt-2 text-gray-600">Choose an action below.</p>

        <div className="mt-8 grid grid-cols-1 sm:grid-cols-3 gap-4">
          <Link to="/upload" className="p-6 bg-white rounded shadow hover:shadow-lg">
            <h3 className="font-medium">Upload File</h3>
            <p className="text-sm text-gray-500 mt-2">Upload files to S3 using presigned URLs.</p>
          </Link>
          <Link to="/files" className="p-6 bg-white rounded shadow hover:shadow-lg">
            <h3 className="font-medium">View Files</h3>
            <p className="text-sm text-gray-500 mt-2">See status and metadata stored in DynamoDB.</p>
          </Link>
          <div className="p-6 bg-white rounded shadow">
            <h3 className="font-medium">Account</h3>
            <p className="text-sm text-gray-500 mt-2">Manage account or sign out via navbar.</p>
          </div>
        </div>
      </div>
    </div>
  );
}
