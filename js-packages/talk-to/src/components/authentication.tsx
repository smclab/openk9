import React from "react";
import { kc } from "../auth/kc";

export const keycloakInit = kc.init({ onLoad: "check-sso" });

type AuthenticationContextValue = { isAuthenticated: boolean };

const AuthenticationContext = React.createContext<AuthenticationContextValue>(null as any);

export function AuthenticationProvider({ children }: { children: React.ReactNode }) {
	const [value, setValue] = React.useState<AuthenticationContextValue>({ isAuthenticated: false });

	React.useEffect(() => {
		keycloakInit.then((isAuthenticated) => setValue({ isAuthenticated }));
	}, []);

	return <AuthenticationContext.Provider value={value}>{children}</AuthenticationContext.Provider>;
}

export async function getUserProfile(): Promise<any> {
	return await kc.loadUserInfo();
}

export function useAuthentication() {
	return React.useContext(AuthenticationContext);
}
