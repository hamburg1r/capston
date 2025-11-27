import { useAuth } from "react-oidc-context";
import { Navigate, Routes, Route } from "react-router-dom";
import HomePage from "./pages/HomePage";
import WelcomePage from "./pages/WelcomePage";
import FileUploadPage from "./pages/FileUploadPage";
import FileListPage from "./pages/FileListPage";
import UploadSuccessPage from "./pages/UploadSuccessPage";
import UploadFailurePage from "./pages/UploadFailurePage";
import ProtectedRoute from "./components/ProtectedRoute";
import LoginPage from "./Pages/LoginPage";
import SignupPage from "./Pages/SignupPage";

export default function App() {
  const auth = useAuth();

  return (
    <Routes>
      {/* If logged in → go to welcome */}
      <Route
        path="/"
        element={
          auth.isAuthenticated ? <Navigate to="/welcome" /> : <HomePage />
        }
      />

      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />

      <Route
        path="/welcome"
        element={
          <ProtectedRoute>
            <WelcomePage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/upload"
        element={
          <ProtectedRoute>
            <FileUploadPage />
          </ProtectedRoute>
        }
      />

      <Route
        path="/files"
        element={
          <ProtectedRoute>
            <FileListPage />
          </ProtectedRoute>
        }
      />

      <Route path="/upload-success" element={<UploadSuccessPage />} />
      <Route path="/upload-failure" element={<UploadFailurePage />} />

      {/* Catch-all */}
      <Route path="*" element={<Navigate to="/" />} />
    </Routes>
  );
}
