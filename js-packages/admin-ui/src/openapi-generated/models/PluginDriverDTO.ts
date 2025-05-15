/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { PluginDriverType } from './PluginDriverType';
import type { Provisioning } from './Provisioning';

export type PluginDriverDTO = {
    name: string;
    description?: string;
    type: PluginDriverType;
    provisioning?: Provisioning;
    jsonConfig?: string;
};
