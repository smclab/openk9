/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { Datasource } from './Datasource';
import type { EventType } from './EventType';

export type K9EntityEventDatasource = {
    type?: EventType;
    entity?: Datasource;
    previousEntity?: Datasource;
};
