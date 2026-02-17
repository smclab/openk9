/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { BehaviorMergeType } from './BehaviorMergeType';
import type { BehaviorOnError } from './BehaviorOnError';
import type { EnrichItemType } from './EnrichItemType';
import type { OffsetDateTime } from './OffsetDateTime';
import type { ResourceUri } from './ResourceUri';

export type EnrichItem = {
    id?: number;
    createDate?: OffsetDateTime;
    modifiedDate?: OffsetDateTime;
    name?: string;
    description?: string;
    type?: EnrichItemType;
    resourceUri?: ResourceUri;
    script?: string;
    jsonConfig?: string;
    jsonPath?: string;
    behaviorMergeType?: BehaviorMergeType;
    requestTimeout?: number;
    behaviorOnError?: BehaviorOnError;
};
