/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { DataIndex } from './DataIndex';
import type { EventType } from './EventType';

export type K9EntityEventDataIndex = {
    type?: EventType;
    entity?: DataIndex;
    previousEntity?: DataIndex;
};
