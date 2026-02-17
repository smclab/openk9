/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { JobStatus } from './JobStatus';
import type { SchedulingStatus } from './SchedulingStatus';

export type TriggerResponse = {
    oldJobStatus?: JobStatus;
    status?: SchedulingStatus;
};
