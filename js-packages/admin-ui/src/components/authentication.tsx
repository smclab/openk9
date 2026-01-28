/*
* Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
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
export async function getUserProfile() {
  return await keycloak.loadUserInfo();
}

export function useAuthentication() {
  return React.useContext(AuthenticationContext);
}

