import React from "react";
import { Link } from "react-router-dom";
import { useAuth } from "react-oidc-context";
import { useDispatch } from "react-redux";
import { clearAuth } from "../store/slices/authSlice";
import "../styles/Navbar.css";

export default function Navbar() {
  const auth = useAuth();
  const dispatch = useDispatch();

  const handleSignIn = () => {
    if (!auth.isAuthenticated && !auth.isLoading) {
      auth.signinRedirect();
    }
  };

  const handleSignUp = () => {
    const clientId = "1v2f3obbse4atof6qb0krmrqnu";
    const domain =
      "https://us-east-1eoyo85ex9.auth.us-east-1.amazoncognito.com";

    const signupUrl = `${domain}/signup?client_id=${clientId}&response_type=code&redirect_uri=http://localhost:5173/login`;

    window.location.href = signupUrl;
  };



  const handleSignOut = async () => {
    await auth.removeUser();
    dispatch(clearAuth());

    const logoutUri = window.location.origin + "/";
    const clientId = "1v2f3obbse4atof6qb0krmrqnu";
    const domain =
      "https://us-east-1eoyo85ex9.auth.us-east-1.amazoncognito.com";

    window.location.href = `${domain}/logout?client_id=${clientId}&logout_uri=${encodeURIComponent(
    logoutUri
  )}`;
  };


  return (
    <nav className="navbar">
      <div className="nav-left">
        <Link to="/" className="nav-logo">
          DocManager
        </Link>

        {auth.isAuthenticated && (
          <div className="nav-links">
            <Link to="/welcome" className="nav-link">
              Welcome
            </Link>
            <Link to="/upload" className="nav-link">
              Upload
            </Link>
            <Link to="/files" className="nav-link">
              Files
            </Link>
          </div>
        )}
      </div>

      <div className="nav-right">
        {auth.isAuthenticated ? (
          <>
            <span className="nav-user">{auth.user?.profile?.email}</span>
            <button className="nav-logout" onClick={handleSignOut}>
              Sign out
            </button>
          </>
        ) : (
          <div className="btn-container">
         <button className="nav-login-btn" onClick={handleSignIn}>
              Sign in
          </button>
         <button className="nav-sign-btn" onClick={handleSignUp}>
               Sign up
          </button>
          </div>
        )}
      </div>
    </nav>
  );
}
