import { UserManager, WebStorageStateStore, type IdTokenClaims } from "oidc-client-ts";
import { resolveTenantUrl } from "../config/tenant";

const OAUTH2_SETTINGS_ENDPOINT = "/api/datasource/oauth2/settings";

type OauthConfig = {
	issuerUri: string;
	clientId: string;
};

type OauthSettingsResponse = {
	issuerUri?: string | null;
	clientId?: string | null;
	clientSecret?: string | null;
};

let userManager: UserManager | null = null;
let authenticated = false;
let initPromise: Promise<boolean> | null = null;

async function loadOauthConfig(): Promise<OauthConfig | null> {
	try {
		const response = await fetch(resolveTenantUrl(OAUTH2_SETTINGS_ENDPOINT), { credentials: "same-origin" });
		if (!response.ok) return null;
		const data = (await response.json()) as OauthSettingsResponse;
		if (!data.issuerUri || data.issuerUri === "DISABLED" || !data.clientId) return null;
		return { issuerUri: data.issuerUri, clientId: data.clientId };
	} catch {
		return null;
	}
}

function buildUserManager(config: OauthConfig): UserManager {
	const redirectUri = window.location.origin + window.location.pathname;
	return new UserManager({
		authority: config.issuerUri,
		client_id: config.clientId,
		redirect_uri: redirectUri,
		post_logout_redirect_uri: redirectUri,
		response_type: "code",
		scope: "openid",
		automaticSilentRenew: true,
		loadUserInfo: true,
		monitorSession: false,
		userStore: new WebStorageStateStore({ store: window.sessionStorage }),
		stateStore: new WebStorageStateStore({ store: window.sessionStorage }),
	});
}

function bindUserManagerEvents(manager: UserManager) {
	manager.events.addUserLoaded(() => {
		authenticated = true;
	});
	manager.events.addUserUnloaded(() => {
		authenticated = false;
	});
	manager.events.addUserSignedOut(() => {
		authenticated = false;
	});
	manager.events.addAccessTokenExpired(() => {
		authenticated = false;
	});
}

function isRedirectCallback(): boolean {
	const params = new URLSearchParams(window.location.search);
	return params.has("code") && params.has("state");
}

/**
 * Loads the OAuth2 settings of the current tenant and initializes the UserManager.
 * Resolves to true when a valid user is available. Anonymous chat stays supported,
 * so a missing session never triggers a redirect: the login is explicit.
 */
export function initOAuth2(): Promise<boolean> {
	if (initPromise) return initPromise;

	initPromise = (async () => {
		const config = await loadOauthConfig();
		if (!config) {
			userManager = null;
			authenticated = false;
			return false;
		}

		userManager = buildUserManager(config);
		bindUserManagerEvents(userManager);

		try {
			if (isRedirectCallback()) {
				await userManager.signinRedirectCallback();
				window.history.replaceState({}, document.title, window.location.pathname);
			}
			const user = await userManager.getUser();
			authenticated = !!user && !user.expired;
			return authenticated;
		} catch (error) {
			console.error("[auth] OAuth2 init failed", error);
			await userManager.removeUser();
			authenticated = false;
			return false;
		}
	})();

	return initPromise;
}

export function isAuthenticated(): boolean {
	return authenticated;
}

export async function getAccessToken(): Promise<string | null> {
	if (!userManager) return null;
	const user = await userManager.getUser();
	if (!user || user.expired) return null;
	return user.access_token ?? null;
}

export async function login(): Promise<void> {
	await userManager?.signinRedirect();
}

export async function logout(): Promise<void> {
	await userManager?.signoutRedirect();
}

export async function loadUserProfile(): Promise<IdTokenClaims | null> {
	const user = await userManager?.getUser();
	return user?.profile ?? null;
}
