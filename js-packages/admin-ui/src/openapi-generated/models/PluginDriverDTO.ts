/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { PluginDriverType } from './PluginDriverType';

export type PluginDriverDTO = {
    name: string;
    description?: string;
    type: PluginDriverType;
    jsonConfig?: string;
};
