/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { Datasource } from './Datasource';
import type { PluginDriver } from './PluginDriver';

export type Tuple2DatasourcePluginDriver = {
    left?: Datasource;
    right?: PluginDriver;
};
