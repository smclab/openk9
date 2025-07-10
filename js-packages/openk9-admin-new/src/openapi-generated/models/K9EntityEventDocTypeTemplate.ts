/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { DocTypeTemplate } from './DocTypeTemplate';
import type { EventType } from './EventType';

export type K9EntityEventDocTypeTemplate = {
    type?: EventType;
    entity?: DocTypeTemplate;
    previousEntity?: DocTypeTemplate;
};
