import React from "react";
import Navbar from "../components/Navbar";
import "../styles/SignupPage.css";

export default function SignupPage() {
  const clientId = "1v2f3obbse4atof6qb0krmrqnu";
  const domain = "https://us-east-1eoyo85ex9.auth.us-east-1.amazoncognito.com";
  const signupUrl = `${domain}/signup?client_id=${clientId}&response_type=code&redirect_uri=${"http://localhost:5173/login"}`;

  return (
    <div className="signup-page">
      <Navbar />

      <div className="signup-card">
        <h2 className="signup-title">Create Your Account</h2>

        <a href={signupUrl} className="full-width-link">
          <button className="signup-btn">Open Hosted UI – Sign Up</button>
        </a>

        <p className="signup-note">
          If your account doesn’t exist, Cognito will guide you through sign-up.
        </p>
      </div>
    </div>
  );
}
