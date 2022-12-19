/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { EnrichItem } from './EnrichItem';
import type { EnrichPipeline } from './EnrichPipeline';

export type Tuple2EnrichPipelineEnrichItem = {
    left?: EnrichPipeline;
    right?: EnrichItem;
};
