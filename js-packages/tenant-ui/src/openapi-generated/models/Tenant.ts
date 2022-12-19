/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { OffsetDateTime } from './OffsetDateTime';

export type Tenant = {
    id?: number;
    createDate?: OffsetDateTime;
    modifiedDate?: OffsetDateTime;
    schemaName?: string;
    virtualHost?: string;
    clientId?: string;
    clientSecret?: string;
    realmName?: string;
};
