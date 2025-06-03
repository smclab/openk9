/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { Preset } from './Preset';

export type CreateConnectorRequest = {
    tenantName: string;
    preset?: Preset;
};
