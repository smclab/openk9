/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { EnrichPipeline } from '../models/EnrichPipeline';
import type { EnrichPipelineDTO } from '../models/EnrichPipelineDTO';
import type { K9Column } from '../models/K9Column';
import type { K9EntityEventEnrichPipeline } from '../models/K9EntityEventEnrichPipeline';
import type { PageEnrichItem } from '../models/PageEnrichItem';
import type { PageEnrichPipeline } from '../models/PageEnrichPipeline';
import type { Tuple2EnrichPipelineEnrichItem } from '../models/Tuple2EnrichPipelineEnrichItem';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class EnrichPipelineResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @param afterId 
     * @param beforeId 
     * @param limit 
     * @param searchText 
     * @param sortBy 
     * @returns PageEnrichPipeline OK
     * @throws ApiError
     */
    public getEnrichPipelines(
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageEnrichPipeline> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/enrich-pipelines',
            query: {
                'after_id': afterId,
                'before_id': beforeId,
                'limit': limit,
                'searchText': searchText,
                'sortBy': sortBy,
            },
        });
    }

    /**
     * @param requestBody 
     * @returns EnrichPipeline OK
     * @throws ApiError
     */
    public postEnrichPipelines(
requestBody?: EnrichPipelineDTO,
): CancelablePromise<EnrichPipeline> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/enrich-pipelines',
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @returns number OK
     * @throws ApiError
     */
    public getEnrichPipelinesCount(): CancelablePromise<number> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/enrich-pipelines/count',
        });
    }

    /**
     * @returns K9EntityEventEnrichPipeline OK
     * @throws ApiError
     */
    public getEnrichPipelinesStream(): CancelablePromise<Array<K9EntityEventEnrichPipeline>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/enrich-pipelines/stream',
        });
    }

    /**
     * @param id 
     * @returns EnrichPipeline OK
     * @throws ApiError
     */
    public getEnrichPipelines1(
id: number,
): CancelablePromise<EnrichPipeline> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/enrich-pipelines/{id}',
            path: {
                'id': id,
            },
        });
    }

    /**
     * @param id 
     * @param requestBody 
     * @returns EnrichPipeline OK
     * @throws ApiError
     */
    public putEnrichPipelines(
id: number,
requestBody?: EnrichPipelineDTO,
): CancelablePromise<EnrichPipeline> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/enrich-pipelines/{id}',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @param id 
     * @returns EnrichPipeline OK
     * @throws ApiError
     */
    public deleteEnrichPipelines(
id: number,
): CancelablePromise<EnrichPipeline> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/enrich-pipelines/{id}',
            path: {
                'id': id,
            },
        });
    }

    /**
     * @param id 
     * @param requestBody 
     * @returns EnrichPipeline OK
     * @throws ApiError
     */
    public patchEnrichPipelines(
id: number,
requestBody?: EnrichPipelineDTO,
): CancelablePromise<EnrichPipeline> {
        return this.httpRequest.request({
            method: 'PATCH',
            url: '/enrich-pipelines/{id}',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @param id 
     * @param afterId 
     * @param beforeId 
     * @param limit 
     * @param searchText 
     * @param sortBy 
     * @returns PageEnrichItem OK
     * @throws ApiError
     */
    public getEnrichPipelinesEnrichItems(
id: number,
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageEnrichItem> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/enrich-pipelines/{id}/enrich-items',
            path: {
                'id': id,
            },
            query: {
                'after_id': afterId,
                'before_id': beforeId,
                'limit': limit,
                'searchText': searchText,
                'sortBy': sortBy,
            },
        });
    }

    /**
     * @param enrichItemId 
     * @param id 
     * @param tail 
     * @returns Tuple2EnrichPipelineEnrichItem OK
     * @throws ApiError
     */
    public putEnrichPipelinesEnrichItems(
enrichItemId: number,
id: number,
tail: boolean = true,
): CancelablePromise<Tuple2EnrichPipelineEnrichItem> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/enrich-pipelines/{id}/enrich-items/{enrichItemId}',
            path: {
                'enrichItemId': enrichItemId,
                'id': id,
            },
            query: {
                'tail': tail,
            },
        });
    }

    /**
     * @param enrichItemId 
     * @param id 
     * @returns Tuple2EnrichPipelineEnrichItem OK
     * @throws ApiError
     */
    public deleteEnrichPipelinesEnrichItems(
enrichItemId: number,
id: number,
): CancelablePromise<Tuple2EnrichPipelineEnrichItem> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/enrich-pipelines/{id}/enrich-items/{enrichItemId}',
            path: {
                'enrichItemId': enrichItemId,
                'id': id,
            },
        });
    }

}
