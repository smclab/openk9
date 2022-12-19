/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { EnrichItemType } from './EnrichItemType';

export type EnrichItemDTO = {
    name: string;
    description?: string;
    type: EnrichItemType;
    serviceName: string;
    validationScript?: string;
    jsonConfig?: string;
};
