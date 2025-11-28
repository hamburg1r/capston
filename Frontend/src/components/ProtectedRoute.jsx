import React from "react";
import { useAuth } from "react-oidc-context";
import { Navigate } from "react-router-dom";
import "../styles/ProtectedRoute.css";

const ProtectedRoute = ({ children }) => {
  const auth = useAuth();

  if (auth.isLoading) {
    return (
      <div className="auth-loading-screen">
        <div className="auth-spinner"></div>
        <p className="auth-loading-text">Verifying your session...</p>
      </div>
    );
  }

  if (!auth.isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return children;
};

export default ProtectedRoute;
