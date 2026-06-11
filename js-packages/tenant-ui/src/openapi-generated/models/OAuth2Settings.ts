/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

/**
 * External OAuth2 identity provider credentials. When provided, Keycloak realm auto-provisioning is skipped.
 */
export type OAuth2Settings = {
    /**
     * OpenID Connect issuer URI.
     */
    issuerUri: string;
    /**
     * OAuth2 client identifier.
     */
    clientId: string;
    /**
     * OAuth2 client secret. Omit for public clients.
     */
    clientSecret?: string;
};

