/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { ChunkType } from './ChunkType';
import type { OffsetDateTime } from './OffsetDateTime';

export type DataIndex = {
    id?: number;
    createDate?: OffsetDateTime;
    modifiedDate?: OffsetDateTime;
    name?: string;
    description?: string;
    knnIndex?: boolean;
    chunkType?: ChunkType;
    chunkWindowSize?: number;
    embeddingJsonConfig?: string;
};
