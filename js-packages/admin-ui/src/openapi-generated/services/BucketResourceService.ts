/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { Bucket } from '../models/Bucket';
import type { BucketDTO } from '../models/BucketDTO';
import type { CurrentBucket } from '../models/CurrentBucket';
import type { DocTypeFieldResponseDTO } from '../models/DocTypeFieldResponseDTO';
import type { K9Column } from '../models/K9Column';
import type { K9EntityEventBucket } from '../models/K9EntityEventBucket';
import type { Language } from '../models/Language';
import type { PageBucket } from '../models/PageBucket';
import type { PageDatasource } from '../models/PageDatasource';
import type { PageSuggestionCategory } from '../models/PageSuggestionCategory';
import type { SortingResponseDTO } from '../models/SortingResponseDTO';
import type { SuggestionCategory } from '../models/SuggestionCategory';
import type { TabResponseDTO } from '../models/TabResponseDTO';
import type { TemplateResponseDTO } from '../models/TemplateResponseDTO';
import type { Tuple2BucketDatasource } from '../models/Tuple2BucketDatasource';
import type { Tuple2BucketSuggestionCategory } from '../models/Tuple2BucketSuggestionCategory';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class BucketResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @param afterId 
     * @param beforeId 
     * @param limit 
     * @param searchText 
     * @param sortBy 
     * @returns PageBucket OK
     * @throws ApiError
     */
    public getApiDatasourceBuckets(
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageBucket> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/buckets',
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
     * @returns Bucket OK
     * @throws ApiError
     */
    public postApiDatasourceBuckets(
requestBody?: BucketDTO,
): CancelablePromise<Bucket> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/buckets',
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @returns number OK
     * @throws ApiError
     */
    public getApiDatasourceBucketsCount(): CancelablePromise<number> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/buckets/count',
        });
    }

    /**
     * @returns CurrentBucket OK
     * @throws ApiError
     */
    public getApiDatasourceBucketsCurrent(): CancelablePromise<CurrentBucket> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/buckets/current',
        });
    }

    /**
     * @returns Language OK
     * @throws ApiError
     */
    public getApiDatasourceBucketsCurrentAvailableLanguage(): CancelablePromise<Array<Language>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/buckets/current/availableLanguage',
        });
    }

    /**
     * @returns Language OK
     * @throws ApiError
     */
    public getApiDatasourceBucketsCurrentDefaultLanguage(): CancelablePromise<Language> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/buckets/current/defaultLanguage',
        });
    }

    /**
     * @param translated 
     * @returns DocTypeFieldResponseDTO OK
     * @throws ApiError
     */
    public getApiDatasourceBucketsCurrentDocTypeFieldsSortable(
translated: boolean = true,
): CancelablePromise<Array<DocTypeFieldResponseDTO>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/buckets/current/doc-type-fields-sortable',
            query: {
                'translated': translated,
            },
        });
    }

    /**
     * @param translated 
     * @returns SortingResponseDTO OK
     * @throws ApiError
     */
    public getApiDatasourceBucketsCurrentSortings(
translated: boolean = true,
): CancelablePromise<Array<SortingResponseDTO>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/buckets/current/sortings',
            query: {
                'translated': translated,
            },
        });
    }

    /**
     * @param translated 
     * @returns SuggestionCategory OK
     * @throws ApiError
     */
    public getApiDatasourceBucketsCurrentSuggestionCategories(
translated: boolean = true,
): CancelablePromise<Array<SuggestionCategory>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/buckets/current/suggestionCategories',
            query: {
                'translated': translated,
            },
        });
    }

    /**
     * @param translated 
     * @returns TabResponseDTO OK
     * @throws ApiError
     */
    public getApiDatasourceBucketsCurrentTabs(
translated: boolean = true,
): CancelablePromise<Array<TabResponseDTO>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/buckets/current/tabs',
            query: {
                'translated': translated,
            },
        });
    }

    /**
     * @returns TemplateResponseDTO OK
     * @throws ApiError
     */
    public getApiDatasourceBucketsCurrentTemplates(): CancelablePromise<Array<TemplateResponseDTO>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/buckets/current/templates',
        });
    }

    /**
     * @returns K9EntityEventBucket OK
     * @throws ApiError
     */
    public getApiDatasourceBucketsStream(): CancelablePromise<Array<K9EntityEventBucket>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/buckets/stream',
        });
    }

    /**
     * @param id 
     * @returns Bucket OK
     * @throws ApiError
     */
    public getApiDatasourceBuckets1(
id: number,
): CancelablePromise<Bucket> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/buckets/{id}',
            path: {
                'id': id,
            },
        });
    }

    /**
     * @param id 
     * @param requestBody 
     * @returns Bucket OK
     * @throws ApiError
     */
    public putApiDatasourceBuckets(
id: number,
requestBody?: BucketDTO,
): CancelablePromise<Bucket> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/api/datasource/buckets/{id}',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @param id 
     * @returns Bucket OK
     * @throws ApiError
     */
    public deleteApiDatasourceBuckets(
id: number,
): CancelablePromise<Bucket> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/api/datasource/buckets/{id}',
            path: {
                'id': id,
            },
        });
    }

    /**
     * @param id 
     * @param requestBody 
     * @returns Bucket OK
     * @throws ApiError
     */
    public patchApiDatasourceBuckets(
id: number,
requestBody?: BucketDTO,
): CancelablePromise<Bucket> {
        return this.httpRequest.request({
            method: 'PATCH',
            url: '/api/datasource/buckets/{id}',
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
     * @returns PageDatasource OK
     * @throws ApiError
     */
    public getApiDatasourceBucketsDatasources(
id: number,
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageDatasource> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/buckets/{id}/datasources',
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
     * @param datasourceId 
     * @param id 
     * @returns Tuple2BucketDatasource OK
     * @throws ApiError
     */
    public putApiDatasourceBucketsDatasources(
datasourceId: number,
id: number,
): CancelablePromise<Tuple2BucketDatasource> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/api/datasource/buckets/{id}/datasources/{datasourceId}',
            path: {
                'datasourceId': datasourceId,
                'id': id,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @param datasourceId 
     * @param id 
     * @returns Tuple2BucketDatasource OK
     * @throws ApiError
     */
    public deleteApiDatasourceBucketsDatasources(
datasourceId: number,
id: number,
): CancelablePromise<Tuple2BucketDatasource> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/api/datasource/buckets/{id}/datasources/{datasourceId}',
            path: {
                'datasourceId': datasourceId,
                'id': id,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @param id 
     * @param afterId 
     * @param beforeId 
     * @param limit 
     * @param searchText 
     * @param sortBy 
     * @returns PageSuggestionCategory OK
     * @throws ApiError
     */
    public getApiDatasourceBucketsSuggestionCategories(
id: number,
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageSuggestionCategory> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/buckets/{id}/suggestion-categories',
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
     * @param suggestionCategoryId 
     * @param requestBody 
     * @returns Tuple2BucketSuggestionCategory OK
     * @throws ApiError
     */
    public putApiDatasourceBucketsSuggestionCategories(
suggestionCategoryId: number,
requestBody?: number,
): CancelablePromise<Tuple2BucketSuggestionCategory> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/api/datasource/buckets/{id}/suggestion-categories/{suggestionCategoryId}',
            path: {
                'suggestionCategoryId': suggestionCategoryId,
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
     * @param suggestionCategoryId 
     * @param requestBody 
     * @returns Tuple2BucketSuggestionCategory OK
     * @throws ApiError
     */
    public deleteApiDatasourceBucketsSuggestionCategories(
suggestionCategoryId: number,
requestBody?: number,
): CancelablePromise<Tuple2BucketSuggestionCategory> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/api/datasource/buckets/{id}/suggestion-categories/{suggestionCategoryId}',
            path: {
                'suggestionCategoryId': suggestionCategoryId,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

}
