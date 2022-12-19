/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { OffsetDateTime } from './OffsetDateTime';
import type { Status } from './Status';
import type { Tenant } from './Tenant';
import type { UUID } from './UUID';

export type BackgroundProcess = {
    id?: UUID;
    createDate?: OffsetDateTime;
    modifiedDate?: OffsetDateTime;
    status?: Status;
    message?: string;
    tenant?: Tenant;
};
