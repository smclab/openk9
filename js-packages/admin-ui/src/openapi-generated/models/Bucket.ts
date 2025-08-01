/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { OffsetDateTime } from './OffsetDateTime';
import type { QueryAnalysis } from './QueryAnalysis';
import type { RetrieveType } from './RetrieveType';
import type { SearchConfig } from './SearchConfig';

export type Bucket = {
    id?: number;
    createDate?: OffsetDateTime;
    modifiedDate?: OffsetDateTime;
    name?: string;
    description?: string;
    refreshOnSuggestionCategory?: boolean;
    refreshOnTab?: boolean;
    refreshOnDate?: boolean;
    refreshOnQuery?: boolean;
    queryAnalysis?: QueryAnalysis;
    searchConfig?: SearchConfig;
    retrieveType?: RetrieveType;
    enabled?: boolean;
};
