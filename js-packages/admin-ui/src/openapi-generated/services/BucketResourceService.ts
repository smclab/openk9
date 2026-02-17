/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { Bucket } from '../models/Bucket';
import type { BucketDTO } from '../models/BucketDTO';
import type { K9Column } from '../models/K9Column';
import type { K9EntityEventBucket } from '../models/K9EntityEventBucket';
import type { PageBucket } from '../models/PageBucket';
import type { PageDatasource } from '../models/PageDatasource';
import type { PageSuggestionCategory } from '../models/PageSuggestionCategory';
import type { Tuple2BucketDatasource } from '../models/Tuple2BucketDatasource';
import type { Tuple2BucketSuggestionCategory } from '../models/Tuple2BucketSuggestionCategory';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class BucketResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @deprecated
     * Persist
     * @param requestBody 
     * @returns Bucket OK
     * @throws ApiError
     */
    public postApiDatasourceBuckets(
requestBody: BucketDTO,
): CancelablePromise<Bucket> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/buckets',
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
    public getApiDatasourceBucketsCount(): CancelablePromise<number> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/buckets/count',
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * Get Current Bucket
     * @returns any Current Bucket configuration returned
     * @throws ApiError
     */
    public getApiDatasourceBucketsCurrent(): CancelablePromise<any> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/buckets/current',
            errors: {
                400: `Bad Request`,
                404: `Not found`,
                500: `Internal Server Error`,
            },
        });
    }

    /**
     * Get Available Language
     * @returns any List of available Languages returned
     * @throws ApiError
     */
    public getApiDatasourceBucketsCurrentAvailableLanguage(): CancelablePromise<any> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/buckets/current/availableLanguage',
            errors: {
                400: `Bad Request`,
                404: `Not found`,
                500: `Internal Server Error`,
            },
        });
    }

    /**
     * Get Default Language
     * @returns any Default Language returned
     * @throws ApiError
     */
    public getApiDatasourceBucketsCurrentDefaultLanguage(): CancelablePromise<any> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/buckets/current/defaultLanguage',
            errors: {
                400: `Bad Request`,
                404: `Not found`,
                500: `Internal Server Error`,
            },
        });
    }

    /**
     * Get Doc Type Fields Sortable
     * @param translated If return translations
     * @returns any List of Sortable DocTypeFields returned
     * @throws ApiError
     */
    public getApiDatasourceBucketsCurrentDocTypeFieldsSortable(
translated: boolean = true,
): CancelablePromise<any> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/buckets/current/doc-type-fields-sortable',
            query: {
                'translated': translated,
            },
            errors: {
                400: `Bad Request`,
                404: `Not found`,
                500: `Internal Server Error`,
            },
        });
    }

    /**
     * Get Sortings
     * @param translated If return translations
     * @returns any List of Sortings returned
     * @throws ApiError
     */
    public getApiDatasourceBucketsCurrentSortings(
translated: boolean = true,
): CancelablePromise<any> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/buckets/current/sortings',
            query: {
                'translated': translated,
            },
            errors: {
                400: `Bad Request`,
                404: `Not found`,
                500: `Internal Server Error`,
            },
        });
    }

    /**
     * Get Suggestion Categories
     * @param translated If return translations
     * @returns any List of Suggestion Categories returned
     * @throws ApiError
     */
    public getApiDatasourceBucketsCurrentSuggestionCategories(
translated: boolean = true,
): CancelablePromise<any> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/buckets/current/suggestionCategories',
            query: {
                'translated': translated,
            },
            errors: {
                400: `Bad Request`,
                404: `Not found`,
                500: `Internal Server Error`,
            },
        });
    }

    /**
     * Get Tabs
     * @param translated If return translations
     * @returns any List of Tabs returned
     * @throws ApiError
     */
    public getApiDatasourceBucketsCurrentTabs(
translated: boolean = true,
): CancelablePromise<any> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/buckets/current/tabs',
            query: {
                'translated': translated,
            },
            errors: {
                400: `Bad Request`,
                404: `Not found`,
                500: `Internal Server Error`,
            },
        });
    }

    /**
     * Get Templates
     * @returns any List of Templates returned
     * @throws ApiError
     */
    public getApiDatasourceBucketsCurrentTemplates(): CancelablePromise<any> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/buckets/current/templates',
            errors: {
                400: `Bad Request`,
                404: `Not found`,
                500: `Internal Server Error`,
            },
        });
    }

    /**
     * @deprecated
     * Get Processor
     * @returns K9EntityEventBucket OK
     * @throws ApiError
     */
    public getApiDatasourceBucketsStream(): CancelablePromise<Array<K9EntityEventBucket>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/buckets/stream',
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
     * @returns Bucket OK
     * @throws ApiError
     */
    public putApiDatasourceBuckets(
id: number,
requestBody: BucketDTO,
): CancelablePromise<Bucket> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/api/datasource/buckets/{id}',
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
     * @returns Bucket OK
     * @throws ApiError
     */
    public patchApiDatasourceBuckets(
id: number,
requestBody: BucketDTO,
): CancelablePromise<Bucket> {
        return this.httpRequest.request({
            method: 'PATCH',
            url: '/api/datasource/buckets/{id}',
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
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @deprecated
     * Get Datasources
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
     * @deprecated
     * Remove Datasource
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
     * @deprecated
     * Add Datasource
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
     * @deprecated
     * Get Suggestion Categories
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
     * @deprecated
     * Remove Suggestion Category
     * @param id 
     * @param suggestionCategoryId 
     * @returns Tuple2BucketSuggestionCategory OK
     * @throws ApiError
     */
    public deleteApiDatasourceBucketsSuggestionCategories(
id: number,
suggestionCategoryId: number,
): CancelablePromise<Tuple2BucketSuggestionCategory> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/api/datasource/buckets/{id}/suggestion-categories/{suggestionCategoryId}',
            path: {
                'id': id,
                'suggestionCategoryId': suggestionCategoryId,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @deprecated
     * Add Suggestion Category
     * @param id 
     * @param suggestionCategoryId 
     * @returns Tuple2BucketSuggestionCategory OK
     * @throws ApiError
     */
    public putApiDatasourceBucketsSuggestionCategories(
id: number,
suggestionCategoryId: number,
): CancelablePromise<Tuple2BucketSuggestionCategory> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/api/datasource/buckets/{id}/suggestion-categories/{suggestionCategoryId}',
            path: {
                'id': id,
                'suggestionCategoryId': suggestionCategoryId,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

}
