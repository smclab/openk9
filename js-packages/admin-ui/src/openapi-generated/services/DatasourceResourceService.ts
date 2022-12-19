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
     * @param afterId 
     * @param beforeId 
     * @param limit 
     * @param searchText 
     * @param sortBy 
     * @returns PageDatasource OK
     * @throws ApiError
     */
    public getDatasources(
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageDatasource> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/datasources',
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
     * @returns Datasource OK
     * @throws ApiError
     */
    public postDatasources(
requestBody?: DatasourceDTO,
): CancelablePromise<Datasource> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/datasources',
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @returns number OK
     * @throws ApiError
     */
    public getDatasourcesCount(): CancelablePromise<number> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/datasources/count',
        });
    }

    /**
     * @returns K9EntityEventDatasource OK
     * @throws ApiError
     */
    public getDatasourcesStream(): CancelablePromise<Array<K9EntityEventDatasource>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/datasources/stream',
        });
    }

    /**
     * @param id 
     * @returns Datasource OK
     * @throws ApiError
     */
    public getDatasources1(
id: number,
): CancelablePromise<Datasource> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/datasources/{id}',
            path: {
                'id': id,
            },
        });
    }

    /**
     * @param id 
     * @param requestBody 
     * @returns Datasource OK
     * @throws ApiError
     */
    public putDatasources(
id: number,
requestBody?: DatasourceDTO,
): CancelablePromise<Datasource> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/datasources/{id}',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @param id 
     * @returns Datasource OK
     * @throws ApiError
     */
    public deleteDatasources(
id: number,
): CancelablePromise<Datasource> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/datasources/{id}',
            path: {
                'id': id,
            },
        });
    }

    /**
     * @param id 
     * @param requestBody 
     * @returns Datasource OK
     * @throws ApiError
     */
    public patchDatasources(
id: number,
requestBody?: DatasourceDTO,
): CancelablePromise<Datasource> {
        return this.httpRequest.request({
            method: 'PATCH',
            url: '/datasources/{id}',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @param id 
     * @returns Datasource OK
     * @throws ApiError
     */
    public deleteDatasourcesDataIndex(
id: number,
): CancelablePromise<Datasource> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/datasources/{id}/data-index',
            path: {
                'id': id,
            },
        });
    }

    /**
     * @param dataIndexId 
     * @param id 
     * @returns DataIndex OK
     * @throws ApiError
     */
    public getDatasourcesDataIndex(
dataIndexId: number,
id: number,
): CancelablePromise<DataIndex> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/datasources/{id}/data-index/{dataIndexId}',
            path: {
                'dataIndexId': dataIndexId,
                'id': id,
            },
        });
    }

    /**
     * @param dataIndexId 
     * @param id 
     * @returns Tuple2DatasourceDataIndex OK
     * @throws ApiError
     */
    public putDatasourcesDataIndex(
dataIndexId: number,
id: number,
): CancelablePromise<Tuple2DatasourceDataIndex> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/datasources/{id}/data-index/{dataIndexId}',
            path: {
                'dataIndexId': dataIndexId,
                'id': id,
            },
        });
    }

    /**
     * @param id 
     * @returns EnrichPipeline OK
     * @throws ApiError
     */
    public getDatasourcesEnrichPipeline(
id: number,
): CancelablePromise<EnrichPipeline> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/datasources/{id}/enrich-pipeline',
            path: {
                'id': id,
            },
        });
    }

    /**
     * @param id 
     * @returns Datasource OK
     * @throws ApiError
     */
    public deleteDatasourcesEnrichPipeline(
id: number,
): CancelablePromise<Datasource> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/datasources/{id}/enrich-pipeline',
            path: {
                'id': id,
            },
        });
    }

    /**
     * @param enrichPipelineId 
     * @param id 
     * @returns Tuple2DatasourceEnrichPipeline OK
     * @throws ApiError
     */
    public putDatasourcesEnrichPipeline(
enrichPipelineId: number,
id: number,
): CancelablePromise<Tuple2DatasourceEnrichPipeline> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/datasources/{id}/enrich-pipeline/{enrichPipelineId}',
            path: {
                'enrichPipelineId': enrichPipelineId,
                'id': id,
            },
        });
    }

    /**
     * @param id 
     * @returns PluginDriver OK
     * @throws ApiError
     */
    public getDatasourcesPluginDriver(
id: number,
): CancelablePromise<PluginDriver> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/datasources/{id}/plugin-driver',
            path: {
                'id': id,
            },
        });
    }

    /**
     * @param id 
     * @returns Datasource OK
     * @throws ApiError
     */
    public deleteDatasourcesPluginDriver(
id: number,
): CancelablePromise<Datasource> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/datasources/{id}/plugin-driver',
            path: {
                'id': id,
            },
        });
    }

    /**
     * @param id 
     * @param pluginDriverId 
     * @returns Tuple2DatasourcePluginDriver OK
     * @throws ApiError
     */
    public putDatasourcesPluginDriver(
id: number,
pluginDriverId: number,
): CancelablePromise<Tuple2DatasourcePluginDriver> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/datasources/{id}/plugin-driver/{pluginDriverId}',
            path: {
                'id': id,
                'pluginDriverId': pluginDriverId,
            },
        });
    }

}
