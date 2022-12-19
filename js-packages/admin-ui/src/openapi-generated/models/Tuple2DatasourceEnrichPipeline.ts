/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { Datasource } from './Datasource';
import type { EnrichPipeline } from './EnrichPipeline';

export type Tuple2DatasourceEnrichPipeline = {
    left?: Datasource;
    right?: EnrichPipeline;
};
