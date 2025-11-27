import React, { createContext } from "react";

export const AppConfigContext = createContext({
  apiBase: import.meta.env.REACT_APP_API_BASE || "http://localhost:8081",
  s3Bucket: import.meta.env.REACT_APP_S3_BUCKET || "quickdeploy-documents",
});

const AppConfigProvider = ({ children }) => {
  const config = {
    apiBase:  import.meta.env.REACT_APP_API_BASE || "http://localhost:8081",
    s3Bucket:  import.meta.env.REACT_APP_S3_BUCKET || "quickdeploy-documents",
  };


  return (
    <AppConfigContext.Provider value={config}>
      {children}
    </AppConfigContext.Provider>
  );
};

export default AppConfigProvider;
