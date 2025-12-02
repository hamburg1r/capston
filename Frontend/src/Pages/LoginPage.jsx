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

    if (auth.error) {
      navigate("/signup");
    }
  }, [auth.user, auth.error, auth.isLoading, dispatch, navigate]);

  return (<>
      <Navbar />
    <div className="login-page">
      <div className="login-card">
        <h2 className="login-title">Redirecting to Login...</h2>
        <p className="login-note">Please wait while we connect you securely.</p>
      </div>
    </div>
  </>
  );
}
