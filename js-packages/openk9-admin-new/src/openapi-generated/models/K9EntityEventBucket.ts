/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { Bucket } from './Bucket';
import type { EventType } from './EventType';

export type K9EntityEventBucket = {
    type?: EventType;
    entity?: Bucket;
    previousEntity?: Bucket;
};
