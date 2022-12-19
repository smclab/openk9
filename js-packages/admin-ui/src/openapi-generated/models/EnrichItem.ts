/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { EnrichItemType } from './EnrichItemType';
import type { OffsetDateTime } from './OffsetDateTime';

export type EnrichItem = {
    createDate?: OffsetDateTime;
    modifiedDate?: OffsetDateTime;
    id?: number;
    name?: string;
    description?: string;
    type?: EnrichItemType;
    serviceName?: string;
    validationScript?: string;
    jsonConfig?: string;
};
