import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { fetchFiles } from "../store/slices/filesSlice";
import { useAuth } from "react-oidc-context";
import "../styles/FileList.css";

export default function FileList() {
  const dispatch = useDispatch();
  const auth = useAuth();
  const filesState = useSelector((s) => s.files);

  useEffect(() => {
    if (auth.isAuthenticated) {
      dispatch(fetchFiles({ token: auth.user?.id_token }));
    }
  }, [auth.isAuthenticated, auth.user, dispatch]);

  if (filesState.loading)
    return <div className="filelist-loading">Loading files...</div>;

  if (filesState.error)
    return <div className="filelist-error">Error: {filesState.error}</div>;

  return (
    <div className="filelist-wrapper">
      <ul className="filelist">
        {filesState.items.length === 0 && (
          <li className="filelist-empty">No files yet</li>
        )}

        {filesState.items.map((f) => (
          <li key={f.documentId} className="file-item-row">
            <div className="file-info">
              <div className="file-name">{f.fileName}</div>
              <div className="file-meta">
                {f.fileType} • {Math.round(f.fileSize / 1024)} KB
              </div>
            </div>

            <div className="file-status-wrapper">
              <span className={`file-status ${f.status.toLowerCase()}`}>
                {f.status}
              </span>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}
