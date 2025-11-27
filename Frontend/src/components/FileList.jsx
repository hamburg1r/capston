import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { fetchFiles } from "../store/slices/filesSlice";
import { useAuth } from "react-oidc-context";

export default function FileList() {
  const dispatch = useDispatch();
  const auth = useAuth();
  const filesState = useSelector((s) => s.files);

  useEffect(() => {
    if (auth.isAuthenticated) {
      dispatch(fetchFiles({ token: auth.user?.access_token }));
    }
  }, [auth.isAuthenticated, auth.user, dispatch]);

  if (filesState.loading) return <div className="p-6">Loading files...</div>;
  if (filesState.error) return <div className="p-6 text-red-600">Error: {filesState.error}</div>;

  return (
    <div className="p-6">
      <ul className="space-y-3">
        {filesState.items.length === 0 && <li>No files yet</li>}
        {filesState.items.map((f) => (
          <li key={f.documentId} className="p-4 bg-white rounded shadow flex justify-between items-center">
            <div>
              <div className="font-medium">{f.fileName}</div>
              <div className="text-sm text-gray-500">{f.fileType} • {Math.round(f.fileSize / 1024)} KB</div>
            </div>
            <div className="text-sm">
              <div className={`px-3 py-1 rounded ${f.status === "COMPLETED" ? "bg-green-100" : f.status === "PROCESSING" ? "bg-yellow-100" : "bg-red-100"}`}>
                {f.status}
              </div>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}
