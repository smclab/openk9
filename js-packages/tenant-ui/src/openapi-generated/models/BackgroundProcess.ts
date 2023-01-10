/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { OffsetDateTime } from './OffsetDateTime';
import type { Status } from './Status';
import type { UUID } from './UUID';

export type BackgroundProcess = {
    id?: number;
    createDate?: OffsetDateTime;
    modifiedDate?: OffsetDateTime;
    status?: Status;
    message?: string;
    processId?: UUID;
    name?: string;
};
