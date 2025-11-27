import React from "react";
import { Link } from "react-router-dom";
import { useAuth } from "react-oidc-context";
import { useDispatch } from "react-redux";
import { clearAuth } from "../store/slices/authSlice";

export default function Navbar() {
  const auth = useAuth();
   const dispatch = useDispatch();

  const handleSignOut = async () => {
    // remove local user and redirect via Cognito hosted logout if needed
    await auth.removeUser();
    dispatch(clearAuth());
    const logoutUri = window.location.origin + "/";
    const clientId = "1v2f3obbse4atof6qb0krmrqnu";
    const domain = "https://us-east-1eoyo85ex9.auth.us-east-1.amazoncognito.com";
    window.location.href = `${domain}/logout?client_id=${clientId}&logout_uri=${encodeURIComponent(
      logoutUri
    )}`;
  };

  return (
    <nav className="bg-slate-800 text-white p-4 flex justify-between items-center">
      <div className="flex items-center gap-4">
        <Link to="/" className="font-bold text-lg">DocManager</Link>
        {auth.isAuthenticated && (
          <>
            <Link to="/welcome" className="hover:underline">Welcome</Link>
            <Link to="/upload" className="hover:underline">Upload</Link>
            <Link to="/files" className="hover:underline">Files</Link>
          </>
        )}
      </div>
      <div>
        {auth.isAuthenticated ? (
          <>
            <span className="mr-4">{auth.user?.profile?.email}</span>
            <button className="bg-red-600 px-3 py-1 rounded" onClick={handleSignOut}>
              Sign out
            </button>
          </>
        ) : (
          <Link to="/login" className="bg-sky-600 px-3 py-1 rounded">Sign in</Link>
        )}
      </div>
    </nav>
  );
}
