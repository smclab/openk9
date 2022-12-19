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
import type { SuggestionCategory } from '../models/SuggestionCategory';
import type { TabResponseDto } from '../models/TabResponseDto';
import type { TemplateResponseDto } from '../models/TemplateResponseDto';
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
    public getBuckets(
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageBucket> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/buckets',
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
    public postBuckets(
requestBody?: BucketDTO,
): CancelablePromise<Bucket> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/buckets',
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @returns number OK
     * @throws ApiError
     */
    public getBucketsCount(): CancelablePromise<number> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/buckets/count',
        });
    }

    /**
     * @returns SuggestionCategory OK
     * @throws ApiError
     */
    public getBucketsCurrentSuggestionCategories(): CancelablePromise<Array<SuggestionCategory>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/buckets/current/suggestionCategories',
        });
    }

    /**
     * @returns TabResponseDto OK
     * @throws ApiError
     */
    public getBucketsCurrentTabs(): CancelablePromise<Array<TabResponseDto>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/buckets/current/tabs',
        });
    }

    /**
     * @returns TemplateResponseDto OK
     * @throws ApiError
     */
    public getBucketsCurrentTemplates(): CancelablePromise<Array<TemplateResponseDto>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/buckets/current/templates',
        });
    }

    /**
     * @returns K9EntityEventBucket OK
     * @throws ApiError
     */
    public getBucketsStream(): CancelablePromise<Array<K9EntityEventBucket>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/buckets/stream',
        });
    }

    /**
     * @param id 
     * @returns Bucket OK
     * @throws ApiError
     */
    public getBuckets1(
id: number,
): CancelablePromise<Bucket> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/buckets/{id}',
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
    public putBuckets(
id: number,
requestBody?: BucketDTO,
): CancelablePromise<Bucket> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/buckets/{id}',
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
    public deleteBuckets(
id: number,
): CancelablePromise<Bucket> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/buckets/{id}',
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
    public patchBuckets(
id: number,
requestBody?: BucketDTO,
): CancelablePromise<Bucket> {
        return this.httpRequest.request({
            method: 'PATCH',
            url: '/buckets/{id}',
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
    public getBucketsDatasources(
id: number,
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageDatasource> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/buckets/{id}/datasources',
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
     * @param datasourceId 
     * @param id 
     * @returns Tuple2BucketDatasource OK
     * @throws ApiError
     */
    public putBucketsDatasources(
datasourceId: number,
id: number,
): CancelablePromise<Tuple2BucketDatasource> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/buckets/{id}/datasources/{datasourceId}',
            path: {
                'datasourceId': datasourceId,
                'id': id,
            },
        });
    }

    /**
     * @param datasourceId 
     * @param id 
     * @returns Tuple2BucketDatasource OK
     * @throws ApiError
     */
    public deleteBucketsDatasources(
datasourceId: number,
id: number,
): CancelablePromise<Tuple2BucketDatasource> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/buckets/{id}/datasources/{datasourceId}',
            path: {
                'datasourceId': datasourceId,
                'id': id,
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
    public getBucketsSuggestionCategories(
id: number,
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageSuggestionCategory> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/buckets/{id}/suggestion-categories',
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
     * @param suggestionCategoryId 
     * @param requestBody 
     * @returns Tuple2BucketSuggestionCategory OK
     * @throws ApiError
     */
    public putBucketsSuggestionCategories(
suggestionCategoryId: number,
requestBody?: number,
): CancelablePromise<Tuple2BucketSuggestionCategory> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/buckets/{id}/suggestion-categories/{suggestionCategoryId}',
            path: {
                'suggestionCategoryId': suggestionCategoryId,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @param suggestionCategoryId 
     * @param requestBody 
     * @returns Tuple2BucketSuggestionCategory OK
     * @throws ApiError
     */
    public deleteBucketsSuggestionCategories(
suggestionCategoryId: number,
requestBody?: number,
): CancelablePromise<Tuple2BucketSuggestionCategory> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/buckets/{id}/suggestion-categories/{suggestionCategoryId}',
            path: {
                'suggestionCategoryId': suggestionCategoryId,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }

}
