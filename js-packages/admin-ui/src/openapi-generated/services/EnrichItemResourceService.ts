/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { EnrichItem } from '../models/EnrichItem';
import type { EnrichItemDTO } from '../models/EnrichItemDTO';
import type { K9Column } from '../models/K9Column';
import type { K9EntityEventEnrichItem } from '../models/K9EntityEventEnrichItem';
import type { PageEnrichItem } from '../models/PageEnrichItem';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class EnrichItemResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @param afterId 
     * @param beforeId 
     * @param limit 
     * @param searchText 
     * @param sortBy 
     * @returns PageEnrichItem OK
     * @throws ApiError
     */
    public getEnrichItems(
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageEnrichItem> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/enrich-items',
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
     * @returns EnrichItem OK
     * @throws ApiError
     */
    public postEnrichItems(
requestBody?: EnrichItemDTO,
): CancelablePromise<EnrichItem> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/enrich-items',
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @returns number OK
     * @throws ApiError
     */
    public getEnrichItemsCount(): CancelablePromise<number> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/enrich-items/count',
        });
    }

    /**
     * @returns K9EntityEventEnrichItem OK
     * @throws ApiError
     */
    public getEnrichItemsStream(): CancelablePromise<Array<K9EntityEventEnrichItem>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/enrich-items/stream',
        });
    }

    /**
     * @param id 
     * @returns EnrichItem OK
     * @throws ApiError
     */
    public getEnrichItems1(
id: number,
): CancelablePromise<EnrichItem> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/enrich-items/{id}',
            path: {
                'id': id,
            },
        });
    }

    /**
     * @param id 
     * @param requestBody 
     * @returns EnrichItem OK
     * @throws ApiError
     */
    public putEnrichItems(
id: number,
requestBody?: EnrichItemDTO,
): CancelablePromise<EnrichItem> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/enrich-items/{id}',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @param id 
     * @returns EnrichItem OK
     * @throws ApiError
     */
    public deleteEnrichItems(
id: number,
): CancelablePromise<EnrichItem> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/enrich-items/{id}',
            path: {
                'id': id,
            },
        });
    }

    /**
     * @param id 
     * @param requestBody 
     * @returns EnrichItem OK
     * @throws ApiError
     */
    public patchEnrichItems(
id: number,
requestBody?: EnrichItemDTO,
): CancelablePromise<EnrichItem> {
        return this.httpRequest.request({
            method: 'PATCH',
            url: '/enrich-items/{id}',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }

}
