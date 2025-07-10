/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { OffsetDateTime } from './OffsetDateTime';

export type Datasource = {
    id?: number;
    createDate?: OffsetDateTime;
    modifiedDate?: OffsetDateTime;
    description?: string;
    jsonConfig?: string;
    lastIngestionDate?: OffsetDateTime;
    name?: string;
    purgeable?: boolean;
    purging?: string;
    purgeMaxAge?: string;
    reindexable?: boolean;
    reindexing?: string;
    schedulable?: boolean;
    scheduling?: string;
};
