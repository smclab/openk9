/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { SecurityConfiguration } from './SecurityConfiguration';

export type TenantResponseDTO = {
    id?: string;
    tenantName?: string;
    virtualHost?: string;
    clientId?: string;
    /**
     * Always null: the client secret is never returned.
     */
    clientSecret?: string;
    issuerUri?: string;
    securityConfiguration?: SecurityConfiguration;
    realmProvisioned?: boolean;
};

