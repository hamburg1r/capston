
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
        <Link to="/" className="nav-logo">DocManager</Link>

        {auth.isAuthenticated && (
          <div className="nav-links">
            <Link to="/welcome" className="nav-link">Welcome</Link>
            <Link to="/upload" className="nav-link">Upload</Link>
            <Link to="/files" className="nav-link">Files</Link>
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
          <>
            <button className="nav-login-btn" onClick={handleSignIn}>
              Sign in
            </button>
            <button className="nav-signup-btn" onClick={handleSignUp}>
              Sign up
            </button>
          </>
        )}
      </div>
    </nav>
  );
}






// my code ......  by default code ...

import React, { useEffect } from "react";
import { useAuth } from "react-oidc-context";
import { useDispatch } from "react-redux";
import { setAuth } from "../store/slices/authSlice";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import "../styles/LoginPage.css";

export default function LoginPage() {
  const auth = useAuth();
  const dispatch = useDispatch();
  const navigate = useNavigate();

  useEffect(() => {
    console.log(auth);
    // When Cognito redirects back with token
    if (auth.user) {
      dispatch(
        setAuth({
          accessToken: auth.user.access_token || auth.user.accessToken,
          idToken: auth.user.id_token,
          profile: auth.user.profile,
        })
      );
      navigate("/welcome", { replace: true });
    }

    // If login failed → send user to Sign Up page
    if (auth.error) {
      navigate("/signup");
    }
  }, [auth.user, auth.error, auth.isLoading, dispatch, navigate]);

  return (
    <div className="login-page">
      <Navbar />
      <div className="login-card">
        <h2 className="login-title">Redirecting to Login...</h2>
        <p className="login-note">Please wait while we connect you securely.</p>
      </div>
    </div>
  );
}


// my old code ....

import React, { useEffect } from "react";
import { useAuth } from "react-oidc-context";
import { useDispatch } from "react-redux";
import { setAuth } from "../store/slices/authSlice";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import "../styles/LoginPage.css";

export default function LoginPage() {
  const auth = useAuth();
  const dispatch = useDispatch();
  const navigate = useNavigate();

  useEffect(() => {
    // when auth gets set from react-oidc-context, save token to redux for app usage
    //  dont use auth.isAuthenticated  , in if  either error  , also remove from dependency array ...
    if (auth.user) {
      console.log("OIDCuserObject:", auth.user);

      dispatch(
        setAuth({
          accessToken: auth.user?.access_token || auth.user?.accessToken,
          idToken: auth.user?.id_token,
          profile: auth.user?.profile,
        })
      );

      navigate("/welcome", { replace: true });
    } else if (auth.error) {
      navigate("/signup");
    }
  }, [auth.user, auth.error, dispatch, navigate]);

  return (
    <div className="login-page">
      <Navbar />

      <div className="login-card">
        <h2 className="login-title">Welcome Back</h2>

        <button
          onClick={() => auth.signinRedirect()}
          className="login-btn"
        >
          Sign in with Cognito
        </button>

        <p className="login-note">
          After successful login, you’ll be redirected to your dashboard.
        </p>
      </div>
    </div>
  );
}
