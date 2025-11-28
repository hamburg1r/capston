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
      console.log("🔵 OIDC user object:", auth.user);

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
