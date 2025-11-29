import React from "react";
import Navbar from "../components/Navbar";
import { useSelector } from "react-redux";
import { Link } from "react-router-dom";
import "../styles/WelcomePage.css";
import user from "../assets/user.png"
export default function WelcomePage() {
  const profile = useSelector((s) => s.auth.profile);
  const authState = useSelector((state) => state.auth);
  console.log("🔍 Redux Auth State:", authState);

  return (
    <div className="welcome-page">
      <Navbar />

      <div className="welcome-container">
        <h2 className="welcome-title">
          Welcome, {profile?.email || profile?.name}
        </h2>
        <p className="welcome-subtitle">Choose an action below.</p>

        <div className="action-grid">

          <Link to="/upload">
          {/* <Link to="/upload" className="action-card"> */}
            {/* <h3 className="action-card-title">Upload File</h3>
            <p className="action-card-desc">
              Upload files to S3 using presigned URLs.
            </p> */}


              <div className="flow-card">
            <img
              src="https://cdn-icons-png.flaticon.com/512/1828/1828490.png"
              alt="Upload"
              className="flow-icon"
            />
            <h3 className="flow-title">Upload File</h3>
            <p className="flow-text">
              choose a file to securely upload.
            </p>
          </div>
          </Link>

          <Link to="/files" className="action-card">
            {/* <h3 className="action-card-title">View Files</h3>
            <p className="action-card-desc">
              See file status and metadata stored in DynamoDB.
            </p> */}

             <div className="flow-card">
            <img
              src="https://cdn-icons-png.flaticon.com/512/4436/4436481.png"
              alt="Done"
              className="flow-icon"
            />
            <h3 className="flow-title">View Documents</h3>
            <p className="flow-text">
              your documents appear here.
            </p>
          </div>
          </Link>

          <div className="action-card disabled flow-card  ">
             <img
              src={user}
              alt="Done"
              className="flow-icon"
            />
            <h3 className="action-card-title  .flow-title">Account</h3>
            <p className="action-card-desc">
              Manage your account or sign out via the navbar.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
