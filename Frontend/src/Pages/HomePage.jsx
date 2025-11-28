import { useAuth } from "react-oidc-context";
import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import "../styles/HomePage.css";

export default function HomePage() {
  const auth = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!auth.isLoading && auth.isAuthenticated) {
      navigate("/welcome", { replace: true });
    }
  }, [auth.isLoading, auth.isAuthenticated, navigate]);

  return (
    <div className="homepage">
      <Navbar />

      <section className="hero-section">
        <div className="hero-content">
          <h1 className="hero-title">Secure File Upload & Manager</h1>
          <p className="hero-subtitle">
            Upload documents securely to AWS S3 and manage them effortlessly.
          </p>

          {!auth.isAuthenticated && (
            <div className="hero-buttons">
              <a href="/login" className="btn btn-primary">Login</a>
              <a href="/signup" className="btn btn-outline">Sign Up</a>
            </div>
          )}
        </div>
      </section>
    </div>
  );
}
