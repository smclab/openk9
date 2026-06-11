import React from "react";
import { render, screen } from "@testing-library/react";
import App from "./App";

test("renders login page under /admin basename", () => {
  // App mounts a BrowserRouter with basename="/admin"; align the jsdom URL so
  // routes match (jsdom defaults to "/", which the router would not resolve).
  window.history.pushState({}, "", "/admin");
  render(<App />);
  // Unauthenticated, RequireAuth redirects to the login page. Assert on a stable
  // single-node heading ("Open"/"K9" branding is split across two <Typography>).
  expect(screen.getByText(/Sign in to Tenant Admin/i)).toBeInTheDocument();
});
