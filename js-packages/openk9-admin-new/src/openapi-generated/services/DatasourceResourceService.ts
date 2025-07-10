/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { DataIndex } from '../models/DataIndex';
import type { Datasource } from '../models/Datasource';
import type { DatasourceDTO } from '../models/DatasourceDTO';
import type { EnrichPipeline } from '../models/EnrichPipeline';
import type { K9Column } from '../models/K9Column';
import type { K9EntityEventDatasource } from '../models/K9EntityEventDatasource';
import type { PageDatasource } from '../models/PageDatasource';
import type { PluginDriver } from '../models/PluginDriver';
import type { Tuple2DatasourceDataIndex } from '../models/Tuple2DatasourceDataIndex';
import type { Tuple2DatasourceEnrichPipeline } from '../models/Tuple2DatasourceEnrichPipeline';
import type { Tuple2DatasourcePluginDriver } from '../models/Tuple2DatasourcePluginDriver';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class DatasourceResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @deprecated
     * Persist
     * @param requestBody 
     * @returns Datasource OK
     * @throws ApiError
     */
    public postApiDatasourceDatasources(
requestBody: DatasourceDTO,
): CancelablePromise<Datasource> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/datasources',
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
     * @returns PageDatasource OK
     * @throws ApiError
     */
    public getApiDatasourceDatasources(
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageDatasource> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/datasources',
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
    public getApiDatasourceDatasourcesCount(): CancelablePromise<number> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/datasources/count',
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @deprecated
     * Get Processor
     * @returns K9EntityEventDatasource OK
     * @throws ApiError
     */
    public getApiDatasourceDatasourcesStream(): CancelablePromise<Array<K9EntityEventDatasource>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/datasources/stream',
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
     * @returns Datasource OK
     * @throws ApiError
     */
    public putApiDatasourceDatasources(
id: number,
requestBody: DatasourceDTO,
): CancelablePromise<Datasource> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/api/datasource/datasources/{id}',
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
     * @returns Datasource OK
     * @throws ApiError
     */
    public patchApiDatasourceDatasources(
id: number,
requestBody: DatasourceDTO,
): CancelablePromise<Datasource> {
        return this.httpRequest.request({
            method: 'PATCH',
            url: '/api/datasource/datasources/{id}',
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
     * @returns Datasource OK
     * @throws ApiError
     */
    public getApiDatasourceDatasources1(
id: number,
): CancelablePromise<Datasource> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/datasources/{id}',
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
     * @returns Datasource OK
     * @throws ApiError
     */
    public deleteApiDatasourceDatasources(
id: number,
): CancelablePromise<Datasource> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/api/datasource/datasources/{id}',
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
     * Unset Data Index
     * @param id 
     * @returns Datasource OK
     * @throws ApiError
     */
    public deleteApiDatasourceDatasourcesDataIndex(
id: number,
): CancelablePromise<Datasource> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/api/datasource/datasources/{id}/data-index',
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
     * Get Data Index Orphans
     * @param id 
     * @returns DataIndex OK
     * @throws ApiError
     */
    public getApiDatasourceDatasourcesDataIndexOrphans(
id: number,
): CancelablePromise<Array<DataIndex>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/datasources/{id}/data-index-orphans',
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
     * Set Data Index
     * @param dataIndexId 
     * @param id 
     * @returns Tuple2DatasourceDataIndex OK
     * @throws ApiError
     */
    public putApiDatasourceDatasourcesDataIndex(
dataIndexId: number,
id: number,
): CancelablePromise<Tuple2DatasourceDataIndex> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/api/datasource/datasources/{id}/data-index/{dataIndexId}',
            path: {
                'dataIndexId': dataIndexId,
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
     * Get Data Index
     * @param dataIndexId 
     * @param id 
     * @returns DataIndex OK
     * @throws ApiError
     */
    public getApiDatasourceDatasourcesDataIndex(
dataIndexId: number,
id: number,
): CancelablePromise<DataIndex> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/datasources/{id}/data-index/{dataIndexId}',
            path: {
                'dataIndexId': dataIndexId,
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
     * Remove Enrich Pipeline
     * @param id 
     * @returns Datasource OK
     * @throws ApiError
     */
    public deleteApiDatasourceDatasourcesEnrichPipeline(
id: number,
): CancelablePromise<Datasource> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/api/datasource/datasources/{id}/enrich-pipeline',
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
     * Get Enrich Pipeline
     * @param id 
     * @returns EnrichPipeline OK
     * @throws ApiError
     */
    public getApiDatasourceDatasourcesEnrichPipeline(
id: number,
): CancelablePromise<EnrichPipeline> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/datasources/{id}/enrich-pipeline',
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
     * Set Enrich Pipeline
     * @param enrichPipelineId 
     * @param id 
     * @returns Tuple2DatasourceEnrichPipeline OK
     * @throws ApiError
     */
    public putApiDatasourceDatasourcesEnrichPipeline(
enrichPipelineId: number,
id: number,
): CancelablePromise<Tuple2DatasourceEnrichPipeline> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/api/datasource/datasources/{id}/enrich-pipeline/{enrichPipelineId}',
            path: {
                'enrichPipelineId': enrichPipelineId,
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
     * Unset Plugin Driver
     * @param id 
     * @returns Datasource OK
     * @throws ApiError
     */
    public deleteApiDatasourceDatasourcesPluginDriver(
id: number,
): CancelablePromise<Datasource> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/api/datasource/datasources/{id}/plugin-driver',
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
     * Get Plugin Driver
     * @param id 
     * @returns PluginDriver OK
     * @throws ApiError
     */
    public getApiDatasourceDatasourcesPluginDriver(
id: number,
): CancelablePromise<PluginDriver> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/datasources/{id}/plugin-driver',
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
     * Set Plugin Driver
     * @param id 
     * @param pluginDriverId 
     * @returns Tuple2DatasourcePluginDriver OK
     * @throws ApiError
     */
    public putApiDatasourceDatasourcesPluginDriver(
id: number,
pluginDriverId: number,
): CancelablePromise<Tuple2DatasourcePluginDriver> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/api/datasource/datasources/{id}/plugin-driver/{pluginDriverId}',
            path: {
                'id': id,
                'pluginDriverId': pluginDriverId,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

}
