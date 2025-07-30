/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { OffsetDateTime } from './OffsetDateTime';
import type { PluginDriverType } from './PluginDriverType';
import type { Provisioning } from './Provisioning';

export type PluginDriver = {
    id?: number;
    createDate?: OffsetDateTime;
    modifiedDate?: OffsetDateTime;
    name?: string;
    description?: string;
    type?: PluginDriverType;
    jsonConfig?: string;
    provisioning?: Provisioning;
};
