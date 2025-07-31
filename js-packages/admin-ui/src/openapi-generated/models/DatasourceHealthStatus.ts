/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { HealthStatus } from './HealthStatus';

export type DatasourceHealthStatus = {
    id?: number;
    name?: string;
    status?: HealthStatus;
};
