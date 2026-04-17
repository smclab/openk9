import React from "react";
import { render, screen } from "@testing-library/react";
import App from "./App";
import { UserManager, WebStorageStateStore } from "oidc-client-ts";

const mockUserManager = new UserManager({
  authority: "http://localhost:8080/realms/test",
  client_id: "test",
  redirect_uri: "http://localhost:3000/tenant/callback",
  userStore: new WebStorageStateStore({ store: sessionStorage }),
});

test("renders openk9", () => {
  render(<App userManager={mockUserManager} />);
  const element = screen.getByText(/OpenK9/i);
  expect(element).toBeInTheDocument();
});
