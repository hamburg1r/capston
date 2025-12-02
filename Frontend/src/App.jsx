import { useAuth } from "react-oidc-context";
import { Navigate, Routes, Route } from "react-router-dom";
import HomePage from "./pages/HomePage";
import FileUploadPage from "./pages/FileUploadPage";
import FileListPage from "./pages/FileListPage";
import UploadSuccessPage from "./pages/UploadSuccessPage";
import UploadFailurePage from "./pages/UploadFailurePage";
import ProtectedRoute from "./components/ProtectedRoute";
import LoginPage from "./Pages/LoginPage";
import SignupPage from "./Pages/SignupPage";
import WelcomePage from "./Pages/WelcomePage";
import FilePreview from "./Pages/FilePreview";

export default function App() {
  const auth = useAuth();

  return (
    <Routes>
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
      <Route
        path="/files/:id"
        element={
          <ProtectedRoute>
            <FilePreview></FilePreview>
          </ProtectedRoute>
        }
      />

      <Route path="/upload-success" element={<UploadSuccessPage />} />
      <Route path="/upload-failure" element={<UploadFailurePage />} />

      <Route path="*" element={<Navigate to="/" />} />
    </Routes>
  );
}
