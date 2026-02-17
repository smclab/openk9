/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { CombinationTechnique } from './CombinationTechnique';
import type { NormalizationTechnique } from './NormalizationTechnique';

export type HybridSearchPipelineDTO = {
    normalizationTechnique?: NormalizationTechnique;
    combinationTechnique?: CombinationTechnique;
    weights?: Array<number>;
};
