/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { OffsetDateTime } from './OffsetDateTime';

export type DatasourceDTO = {
    name: string;
    description?: string;
    scheduling: string;
    lastIngestionDate?: OffsetDateTime;
    schedulable?: boolean;
    jsonConfig?: string;
};
