/*
* Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
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
     * @deprecated
     * Persist
     * @param requestBody 
     * @returns EnrichPipeline OK
     * @throws ApiError
     */
    public postApiDatasourceEnrichPipelines(
requestBody: EnrichPipelineDTO,
): CancelablePromise<EnrichPipeline> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/enrich-pipelines',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Bad Request`,
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @deprecated
     * Find All
     * @param afterId 
     * @param beforeId 
     * @param limit 
     * @param searchText 
     * @param sortBy 
     * @returns PageEnrichPipeline OK
     * @throws ApiError
     */
    public getApiDatasourceEnrichPipelines(
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageEnrichPipeline> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/enrich-pipelines',
            query: {
                'after_id': afterId,
                'before_id': beforeId,
                'limit': limit,
                'searchText': searchText,
                'sortBy': sortBy,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @deprecated
     * Count
     * @returns number OK
     * @throws ApiError
     */
    public getApiDatasourceEnrichPipelinesCount(): CancelablePromise<number> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/enrich-pipelines/count',
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @deprecated
     * Get Processor
     * @returns K9EntityEventEnrichPipeline OK
     * @throws ApiError
     */
    public getApiDatasourceEnrichPipelinesStream(): CancelablePromise<Array<K9EntityEventEnrichPipeline>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/enrich-pipelines/stream',
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @deprecated
     * Update
     * @param id 
     * @param requestBody 
     * @returns EnrichPipeline OK
     * @throws ApiError
     */
    public putApiDatasourceEnrichPipelines(
id: number,
requestBody: EnrichPipelineDTO,
): CancelablePromise<EnrichPipeline> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/api/datasource/enrich-pipelines/{id}',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Bad Request`,
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @deprecated
     * Patch
     * @param id 
     * @param requestBody 
     * @returns EnrichPipeline OK
     * @throws ApiError
     */
    public patchApiDatasourceEnrichPipelines(
id: number,
requestBody: EnrichPipelineDTO,
): CancelablePromise<EnrichPipeline> {
        return this.httpRequest.request({
            method: 'PATCH',
            url: '/api/datasource/enrich-pipelines/{id}',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @deprecated
     * Find By Id
     * @param id 
     * @returns EnrichPipeline OK
     * @throws ApiError
     */
    public getApiDatasourceEnrichPipelines1(
id: number,
): CancelablePromise<EnrichPipeline> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/enrich-pipelines/{id}',
            path: {
                'id': id,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @deprecated
     * Delete By Id
     * @param id 
     * @returns EnrichPipeline OK
     * @throws ApiError
     */
    public deleteApiDatasourceEnrichPipelines(
id: number,
): CancelablePromise<EnrichPipeline> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/api/datasource/enrich-pipelines/{id}',
            path: {
                'id': id,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @deprecated
     * Get Enrich Items
     * @param id 
     * @param afterId 
     * @param beforeId 
     * @param limit 
     * @param searchText 
     * @param sortBy 
     * @returns PageEnrichItem OK
     * @throws ApiError
     */
    public getApiDatasourceEnrichPipelinesEnrichItems(
id: number,
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageEnrichItem> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/enrich-pipelines/{id}/enrich-items',
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
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @deprecated
     * Remove Enrich Item
     * @param enrichItemId 
     * @param id 
     * @returns Tuple2EnrichPipelineEnrichItem OK
     * @throws ApiError
     */
    public deleteApiDatasourceEnrichPipelinesEnrichItems(
enrichItemId: number,
id: number,
): CancelablePromise<Tuple2EnrichPipelineEnrichItem> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/api/datasource/enrich-pipelines/{id}/enrich-items/{enrichItemId}',
            path: {
                'enrichItemId': enrichItemId,
                'id': id,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @deprecated
     * Add Enrich Item
     * @param enrichItemId 
     * @param id 
     * @param tail 
     * @returns Tuple2EnrichPipelineEnrichItem OK
     * @throws ApiError
     */
    public putApiDatasourceEnrichPipelinesEnrichItems(
enrichItemId: number,
id: number,
tail: boolean = true,
): CancelablePromise<Tuple2EnrichPipelineEnrichItem> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/api/datasource/enrich-pipelines/{id}/enrich-items/{enrichItemId}',
            path: {
                'enrichItemId': enrichItemId,
                'id': id,
            },
            query: {
                'tail': tail,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

}

