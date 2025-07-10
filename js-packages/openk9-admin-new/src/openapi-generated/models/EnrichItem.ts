/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { BehaviorMergeType } from './BehaviorMergeType';
import type { BehaviorOnError } from './BehaviorOnError';
import type { EnrichItemType } from './EnrichItemType';
import type { OffsetDateTime } from './OffsetDateTime';

export type EnrichItem = {
    id?: number;
    createDate?: OffsetDateTime;
    modifiedDate?: OffsetDateTime;
    name?: string;
    description?: string;
    type?: EnrichItemType;
    serviceName?: string;
    script?: string;
    jsonConfig?: string;
    jsonPath?: string;
    behaviorMergeType?: BehaviorMergeType;
    requestTimeout?: number;
    behaviorOnError?: BehaviorOnError;
};
