import { useAuth } from "react-oidc-context";
import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";

export default function HomePage() {
  const auth = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    // If user is logged in, redirect them to welcome page
    if (!auth.isLoading && auth.isAuthenticated) {
      navigate("/welcome", { replace: true });
    }
  }, [auth.isLoading, auth.isAuthenticated]);

  // Still show homepage for unauthenticated users
  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-3xl mx-auto py-16 text-center">
        <h1 className="text-4xl font-bold mb-4">Secure File Upload & Manager</h1>
        <p className="mb-8">Upload files to S3 and manage them easily.</p>
        {!auth.isAuthenticated && (
          <div className="flex gap-4 justify-center">
            <a href="/login" className="px-6 py-3 bg-blue-600 text-white rounded">Login</a>
            <a href="/signup" className="px-6 py-3 bg-white border rounded">Sign Up</a>
          </div>
        )}
      </div>
    </div>
  );
}
