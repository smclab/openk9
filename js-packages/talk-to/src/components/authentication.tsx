import React from "react";
import { keycloak } from "./keycloak";

export const keycloakInit = keycloak.init({ onLoad: "check-sso" });
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
export async function getUserProfile(): Promise<any> {
	return await keycloak.loadUserInfo();
}

export function useAuthentication() {
	return React.useContext(AuthenticationContext);
}
