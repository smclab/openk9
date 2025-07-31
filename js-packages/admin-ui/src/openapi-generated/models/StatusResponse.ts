/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { DatasourceHealthStatus } from './DatasourceHealthStatus';

export type StatusResponse = {
    datasources?: Array<DatasourceHealthStatus>;
    total?: number;
    errors?: number;
};
