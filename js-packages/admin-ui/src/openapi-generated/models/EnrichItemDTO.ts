/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { BehaviorMergeType } from './BehaviorMergeType';
import type { BehaviorOnError } from './BehaviorOnError';
import type { EnrichItemType } from './EnrichItemType';
import type { ResourceUri } from './ResourceUri';

export type EnrichItemDTO = {
    name: string;
    description?: string;
    type: EnrichItemType;
    resourceUri: ResourceUri;
    script?: string;
    jsonConfig?: string;
    jsonPath: string;
    behaviorMergeType: BehaviorMergeType;
    requestTimeout: number;
    behaviorOnError: BehaviorOnError;
};
