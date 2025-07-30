/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { RetrieveType } from './RetrieveType';

export type BucketDTO = {
    name: string;
    description?: string;
    refreshOnSuggestionCategory: boolean;
    refreshOnTab: boolean;
    refreshOnDate: boolean;
    refreshOnQuery: boolean;
    retrieveType: RetrieveType;
};
