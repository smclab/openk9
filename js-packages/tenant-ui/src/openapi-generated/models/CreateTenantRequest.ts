/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { IngressScope } from './IngressScope';
import type { OAuth2Settings } from './OAuth2Settings';
import type { SecurityConfiguration } from './SecurityConfiguration';

/**
 * Request payload for creating a new tenant.
 */
export type CreateTenantRequest = {
    /**
     * Hostname that identifies this tenant (e.g. demo.openk9.io). Used as Ingress host and Keycloak realm alias.
     */
    virtualHost: string;
    /**
     * Gateway-level authorization model. Determines which routes require OAuth2, API keys, or no auth.
     */
    securityConfiguration: SecurityConfiguration;
    /**
     * External identity provider credentials. When provided, Keycloak realm provisioning is skipped and these values are stored directly. Omit to auto-provision via Keycloak (if available).
     */
    oAuth2Settings?: OAuth2Settings;
    /**
     * Tenant identifier used as database schema name and Kubernetes resource prefix. Must match [a-z][a-z0-9]{0,62}. Omit to auto-generate.
     */
    tenantName?: string;
    /**
     * Route groups to expose on the Kubernetes Ingress: SEARCH, ADMINISTRATION, RAG, INGESTION. Omit for the default set (SEARCH, ADMINISTRATION, RAG). An empty list skips ingress creation entirely.
     */
    ingressScopes?: Array<IngressScope>;
};

