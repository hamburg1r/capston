import { useAuth } from "react-oidc-context";
import { Navigate } from "react-router-dom";

export default function ProtectedRoute({ children }) {
  const auth = useAuth();

  if (auth.isLoading) return null; // or loader
  

  return auth.isAuthenticated ? children : <Navigate to="/" replace />;
}
