/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { PluginDriverType } from './PluginDriverType';
import type { Provisioning } from './Provisioning';
import type { ResourceUri } from './ResourceUri';

export type PluginDriverDTO = {
    name: string;
    description?: string;
    type: PluginDriverType;
    provisioning?: Provisioning;
    resourceUri?: ResourceUri;
};
