/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { EventType } from './EventType';
import type { PluginDriver } from './PluginDriver';

export type K9EntityEventPluginDriver = {
    type?: EventType;
    entity?: PluginDriver;
    previousEntity?: PluginDriver;
};
