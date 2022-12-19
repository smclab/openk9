/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { EventType } from './EventType';
import type { SuggestionCategory } from './SuggestionCategory';

export type K9EntityEventSuggestionCategory = {
    type?: EventType;
    entity?: SuggestionCategory;
    previousEntity?: SuggestionCategory;
};
