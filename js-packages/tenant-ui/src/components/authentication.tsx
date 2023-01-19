import React from "react";
import Keycloak from "keycloak-js";

export const keycloak = new Keycloak({
  url: window.KEYCLOAK_URL,
  realm: window.KEYCLOAK_REALM,
  clientId: window.KEYCLOAK_CLIENT_ID,
});

export const keycloakInit = keycloak.init({ onLoad: "login-required" });
type AuthenticationContextValue = { isAuthenticated: boolean };

const AuthenticationContext = React.createContext<AuthenticationContextValue>(null as any);

export function AuthenticationProvider({ children }: { children: React.ReactNode }) {
  const [value, setValue] = React.useState<AuthenticationContextValue>({ isAuthenticated: false });
  React.useEffect(() => {
    keycloakInit.then((isAuthenticated) => {
      setValue({ isAuthenticated });
    });
  }, []);
  return <AuthenticationContext.Provider value={value}>{children}</AuthenticationContext.Provider>;
}
export function useAuthentication() {
  return React.useContext(AuthenticationContext);
}

export async function getUserProfile() {
  return await keycloak.loadUserInfo();
}
