import { Route, Routes } from "react-router-dom";
import Dashboard from "../views/Dashboard";
import ProtectedRoute from "./ProtectedRoute";

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<div>Missing</div>} />
      <Route element={<ProtectedRoute isAuthenticated={true} />}>
        <Route path="/dashboard" element={<Dashboard />} />
      </Route>
    </Routes>
  )
}
