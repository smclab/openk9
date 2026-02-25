/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { EnrichItem } from './EnrichItem';
import type { EventType } from './EventType';

export type K9EntityEventEnrichItem = {
    type?: EventType;
    entity?: EnrichItem;
    previousEntity?: EnrichItem;
};
