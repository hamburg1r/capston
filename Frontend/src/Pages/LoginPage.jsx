import React, { useEffect } from "react";
import { useAuth } from "react-oidc-context";
import { useDispatch } from "react-redux";
import { setAuth } from "../store/slices/authSlice";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";

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
    } else if (auth.error) {      navigate("/signup");
    }
  }, [ auth.user, auth.error, dispatch, navigate]);

  return (
    <div>
      <Navbar />
      <div className="max-w-md mx-auto mt-20 p-6 bg-white rounded shadow">
        <h2 className="text-2xl mb-4">Login</h2>
        <button
          onClick={() => auth.signinRedirect()}
          className="w-full py-2 bg-blue-600 text-white rounded"
        >
          Sign in with Cognito
        </button>
        <p className="mt-4 text-sm text-gray-500">After success you'll be forwarded to the app.</p>
      </div>
    </div>
  );
}
