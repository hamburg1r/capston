import React from "react";
import { createRoot } from "react-dom/client";
import { AuthProvider } from "react-oidc-context";
import { Provider } from "react-redux";
import { BrowserRouter } from "react-router-dom";
import App from "./App";
import { store } from "./store/store";
import { cognitoConfig } from "./authConfig";
import AppConfigProvider from "./context/AppConfigContext";
import "./styles/index.css";

createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <AuthProvider {...cognitoConfig}>
      <Provider store={store}>
        <AppConfigProvider>
          <BrowserRouter>
            <App />
          </BrowserRouter>
        </AppConfigProvider>
      </Provider>
    </AuthProvider>
  </React.StrictMode>
);
