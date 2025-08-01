/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { K9Column } from '../models/K9Column';
import type { K9EntityEventSuggestionCategory } from '../models/K9EntityEventSuggestionCategory';
import type { PageDocTypeField } from '../models/PageDocTypeField';
import type { PageSuggestionCategory } from '../models/PageSuggestionCategory';
import type { SuggestionCategory } from '../models/SuggestionCategory';
import type { SuggestionCategoryDTO } from '../models/SuggestionCategoryDTO';
import type { Tuple2SuggestionCategoryDocTypeField } from '../models/Tuple2SuggestionCategoryDocTypeField';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class SuggestionCategoryResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @deprecated
     * Persist
     * @param requestBody 
     * @returns SuggestionCategory OK
     * @throws ApiError
     */
    public postApiDatasourceSuggestionCategories(
requestBody: SuggestionCategoryDTO,
): CancelablePromise<SuggestionCategory> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/suggestion-categories',
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
     * @returns PageSuggestionCategory OK
     * @throws ApiError
     */
    public getApiDatasourceSuggestionCategories(
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageSuggestionCategory> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/suggestion-categories',
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
    public getApiDatasourceSuggestionCategoriesCount(): CancelablePromise<number> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/suggestion-categories/count',
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @deprecated
     * Get Processor
     * @returns K9EntityEventSuggestionCategory OK
     * @throws ApiError
     */
    public getApiDatasourceSuggestionCategoriesStream(): CancelablePromise<Array<K9EntityEventSuggestionCategory>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/suggestion-categories/stream',
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
     * @returns SuggestionCategory OK
     * @throws ApiError
     */
    public putApiDatasourceSuggestionCategories(
id: number,
requestBody: SuggestionCategoryDTO,
): CancelablePromise<SuggestionCategory> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/api/datasource/suggestion-categories/{id}',
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
     * @returns SuggestionCategory OK
     * @throws ApiError
     */
    public patchApiDatasourceSuggestionCategories(
id: number,
requestBody: SuggestionCategoryDTO,
): CancelablePromise<SuggestionCategory> {
        return this.httpRequest.request({
            method: 'PATCH',
            url: '/api/datasource/suggestion-categories/{id}',
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
     * @returns SuggestionCategory OK
     * @throws ApiError
     */
    public getApiDatasourceSuggestionCategories1(
id: number,
): CancelablePromise<SuggestionCategory> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/suggestion-categories/{id}',
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
     * @returns SuggestionCategory OK
     * @throws ApiError
     */
    public deleteApiDatasourceSuggestionCategories(
id: number,
): CancelablePromise<SuggestionCategory> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/api/datasource/suggestion-categories/{id}',
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
     * Unset Doc Type Field
     * @param id 
     * @returns SuggestionCategory OK
     * @throws ApiError
     */
    public deleteApiDatasourceSuggestionCategoriesDocTypeField(
id: number,
): CancelablePromise<SuggestionCategory> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/api/datasource/suggestion-categories/{id}/doc-type-field',
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
     * Get Doc Type Fields
     * @param id 
     * @param afterId 
     * @param beforeId 
     * @param limit 
     * @param searchText 
     * @param sortBy 
     * @returns PageDocTypeField OK
     * @throws ApiError
     */
    public getApiDatasourceSuggestionCategoriesDocTypeFields(
id: number,
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageDocTypeField> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/suggestion-categories/{id}/doc-type-fields',
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
     * Remove Doc Type Field
     * @param docTypeFieldId 
     * @param id 
     * @returns Tuple2SuggestionCategoryDocTypeField OK
     * @throws ApiError
     */
    public deleteApiDatasourceSuggestionCategoriesDocTypeFields(
docTypeFieldId: number,
id: number,
): CancelablePromise<Tuple2SuggestionCategoryDocTypeField> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/api/datasource/suggestion-categories/{id}/doc-type-fields/{docTypeFieldId}',
            path: {
                'docTypeFieldId': docTypeFieldId,
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
     * Add Doc Type Field
     * @param docTypeFieldId 
     * @param id 
     * @returns Tuple2SuggestionCategoryDocTypeField OK
     * @throws ApiError
     */
    public putApiDatasourceSuggestionCategoriesDocTypeFields(
docTypeFieldId: number,
id: number,
): CancelablePromise<Tuple2SuggestionCategoryDocTypeField> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/api/datasource/suggestion-categories/{id}/doc-type-fields/{docTypeFieldId}',
            path: {
                'docTypeFieldId': docTypeFieldId,
                'id': id,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

}
