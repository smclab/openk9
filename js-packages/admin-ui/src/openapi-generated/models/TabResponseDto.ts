/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { SortingResponseDTO } from './SortingResponseDTO';
import type { TokenTabResponseDTO } from './TokenTabResponseDTO';

export type TabResponseDTO = {
    label?: string;
    tokens?: Array<TokenTabResponseDTO>;
    sortings?: Array<SortingResponseDTO>;
    translationMap?: Record<string, string>;
};
