export const useKeycloak =
	(import.meta as any).env?.VITE_USE_KEYCLOAK === "true" || (process as any).env?.REACT_APP_USE_KEYCLOAK === "true";
