/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { OffsetDateTime } from './OffsetDateTime';
import type { PluginDriverType } from './PluginDriverType';
import type { Provisioning } from './Provisioning';

export type PluginDriver = {
    createDate?: OffsetDateTime;
    modifiedDate?: OffsetDateTime;
    id?: number;
    name?: string;
    description?: string;
    type?: PluginDriverType;
    jsonConfig?: string;
    provisioning?: Provisioning;
};
