/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { OffsetDateTime } from './OffsetDateTime';

export type DocType = {
    id?: number;
    createDate?: OffsetDateTime;
    modifiedDate?: OffsetDateTime;
    name?: string;
    description?: string;
};
