/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { DocType } from './DocType';
import type { EventType } from './EventType';

export type K9EntityEventDocType = {
    type?: EventType;
    entity?: DocType;
    previousEntity?: DocType;
};
