/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { OffsetDateTime } from './OffsetDateTime';

export type EnrichPipeline = {
    createDate?: OffsetDateTime;
    modifiedDate?: OffsetDateTime;
    id?: number;
    name?: string;
    description?: string;
};
