import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import reportWebVitals from "./reportWebVitals";
import { fetchOidcSettings, createUserManager } from "./components/client/oidcClient";

const root = ReactDOM.createRoot(document.getElementById("root") as HTMLElement);

/**
 * Bootstrap: fetch OIDC configuration from backend, then create UserManager
 * and render the app. This ensures auth config is available before any
 * component mounts — no conditional rendering or race conditions.
 */
async function bootstrap() {
  try {
    const settings = await fetchOidcSettings();
    const userManager = createUserManager(settings);

    root.render(
      <React.StrictMode>
        <App userManager={userManager} />
      </React.StrictMode>
    );
  } catch (error) {
    console.error("Failed to initialize authentication:", error);
    root.render(
      <div style={{ display: "flex", justifyContent: "center", alignItems: "center", height: "100vh", fontFamily: "sans-serif" }}>
        <div style={{ textAlign: "center" }}>
          <h2>Unable to load authentication configuration</h2>
          <p>Please check your network connection and try again.</p>
          <button onClick={() => window.location.reload()} style={{ padding: "8px 24px", cursor: "pointer" }}>
            Retry
          </button>
        </div>
      </div>
    );
  }
}

bootstrap();

reportWebVitals();
