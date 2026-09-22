import React from "react";
import { initOAuth2, isAuthenticated, loadUserProfile } from "../auth/oauth2";

export const authInit = initOAuth2();

type AuthenticationContextValue = { isAuthenticated: boolean };

const AuthenticationContext = React.createContext<AuthenticationContextValue | null>(null);

export function AuthenticationProvider({ children }: { children: React.ReactNode }) {
	const [value, setValue] = React.useState<AuthenticationContextValue>({ isAuthenticated: isAuthenticated() });

	React.useEffect(() => {
		authInit.then((authenticated) => setValue({ isAuthenticated: authenticated }));
	}, []);

	return <AuthenticationContext.Provider value={value}>{children}</AuthenticationContext.Provider>;
}

export async function getUserProfile() {
	return loadUserProfile();
}

export function useAuthentication() {
	const value = React.useContext(AuthenticationContext);
	if (!value) {
		throw new Error("useAuthentication must be used within an AuthenticationProvider");
	}
	return value;
}
