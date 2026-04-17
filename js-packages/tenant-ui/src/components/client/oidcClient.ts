import { UserManager, WebStorageStateStore } from "oidc-client-ts";

export type OidcSettings = {
  issuerUri: string;
  clientId: string;
  clientSecret: string | null;
};

let userManagerInstance: UserManager | null = null;

/**
 * Fetches OIDC configuration from the backend.
 * This endpoint returns issuerUri, clientId, and clientSecret (null for public clients).
 */
export async function fetchOidcSettings(): Promise<OidcSettings> {
  const response = await fetch("/api/datasource/oauth2/settings");
  if (!response.ok) {
    throw new Error("Failed to fetch OAuth2 settings");
  }
  return response.json();
}

/**
 * Creates and caches a UserManager singleton.
 * Uses Authorization Code Flow + PKCE (response_type: "code").
 * The same instance is shared between react-oidc-context and imperative token access.
 */
export function createUserManager(settings: OidcSettings): UserManager {
  if (userManagerInstance) return userManagerInstance;

  const origin = window.location.origin;
  const basePath = "/tenant";

  userManagerInstance = new UserManager({
    authority: settings.issuerUri,
    client_id: settings.clientId,
    redirect_uri: `${origin}${basePath}/callback`,
    post_logout_redirect_uri: `${origin}${basePath}/`,
    silent_redirect_uri: `${origin}${basePath}/silent-renew.html`,
    response_type: "code",
    scope: "openid profile email",
    automaticSilentRenew: true,
    userStore: new WebStorageStateStore({ store: sessionStorage }),
  });

  return userManagerInstance;
}

export function getUserManager(): UserManager | null {
  return userManagerInstance;
}

/**
 * Imperatively retrieves the current access token.
 * Used by Apollo and REST clients which live outside React context.
 */
export async function getAccessToken(): Promise<string | null> {
  if (!userManagerInstance) return null;
  const user = await userManagerInstance.getUser();
  return user?.access_token ?? null;
}
