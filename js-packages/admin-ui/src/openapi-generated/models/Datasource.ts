/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { OffsetDateTime } from './OffsetDateTime';

export type Datasource = {
    createDate?: OffsetDateTime;
    modifiedDate?: OffsetDateTime;
    id?: number;
    name?: string;
    description?: string;
    scheduling?: string;
    lastIngestionDate?: OffsetDateTime;
    schedulable?: boolean;
    jsonConfig?: string;
};
