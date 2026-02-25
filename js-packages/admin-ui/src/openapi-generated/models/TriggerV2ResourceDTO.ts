/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { OffsetDateTime } from './OffsetDateTime';

export type TriggerV2ResourceDTO = {
    datasourceId?: number;
    reindex?: boolean;
    startIngestionDate?: OffsetDateTime;
};
