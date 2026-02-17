/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { EnrichPipeline } from './EnrichPipeline';
import type { EventType } from './EventType';

export type K9EntityEventEnrichPipeline = {
    type?: EventType;
    entity?: EnrichPipeline;
    previousEntity?: EnrichPipeline;
};
