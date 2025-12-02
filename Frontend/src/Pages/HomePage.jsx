import { useAuth } from "react-oidc-context";
import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import "../styles/HomePage.css";
import Dashboard from '../assets/Dashboard.webp'
import UploadCloud from '../assets/UploadCloud.png'
import FilePic from '../assets/FilePic.png'
import ClounPic from '../assets/ClounPic.png'
import All from '../assets/All.png'
import aay4rykcs from '../assets/aay4rykcs.webp'

export default function HomePage() {
  const auth = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!auth.isLoading && auth.isAuthenticated) {
      navigate("/welcome", { replace: true });
    }
  }, [auth.isLoading, auth.isAuthenticated, navigate]);

  return (
     <div className="home-container">
          <Navbar />
    
          <section className="hero-section">
            <div className="hero-content">
              <h1 className="hero-title">
                Secure. Fast. Reliable.<br />
                <span>Your Document Manager.</span>
              </h1>
    
              <p className="hero-subtitle">
                Upload, organize, preview, and manage your documents seamlessly — powered by AWS.
              </p>
            </div>
    
            <div className="hero-image-wrapper">
              <img 
                src={Dashboard}
                alt="Document Manager Illustration" 
                className="hero-image"
              />
            </div>
          </section>
    
          <section className="features-section">
            <div className="feature-card">
              <img src={UploadCloud} className="feature-icon" />
              <h3>Secure Uploads</h3>
              <p>Upload files directly to AWS S3 using encrypted presigned URLs.</p>
            </div>
    
            <div className="feature-card">
              <img src={aay4rykcs} className="feature-icon" />
              <h3>Cloud Powered</h3>
              <p>Backed by AWS S3, Lambda, DynamoDB for superior reliability.</p>
            </div>
    
            <div className="feature-card">
              <img src={All} className="feature-icon" />
              <h3>Easy Management</h3>
              <p>View, organize, and preview your files in one clean dashboard.</p>
            </div>
          </section>
        </div>  
  );
}
