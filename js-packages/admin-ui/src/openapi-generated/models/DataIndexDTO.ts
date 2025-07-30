/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { ChunkType } from './ChunkType';

export type DataIndexDTO = {
    name: string;
    description?: string;
    knnIndex?: boolean;
    chunkWindowSize?: number;
    chunkType?: ChunkType;
    embeddingDocTypeFieldId?: number;
    embeddingJsonConfig?: string;
    docTypeIds?: Array<number>;
    settings?: string;
};
