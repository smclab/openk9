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
     * @param afterId 
     * @param beforeId 
     * @param limit 
     * @param searchText 
     * @param sortBy 
     * @returns PageSuggestionCategory OK
     * @throws ApiError
     */
    public getSuggestionCategories(
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageSuggestionCategory> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/suggestion-categories',
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
     * @returns SuggestionCategory OK
     * @throws ApiError
     */
    public postSuggestionCategories(
requestBody?: SuggestionCategoryDTO,
): CancelablePromise<SuggestionCategory> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/suggestion-categories',
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @returns number OK
     * @throws ApiError
     */
    public getSuggestionCategoriesCount(): CancelablePromise<number> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/suggestion-categories/count',
        });
    }

    /**
     * @returns K9EntityEventSuggestionCategory OK
     * @throws ApiError
     */
    public getSuggestionCategoriesStream(): CancelablePromise<Array<K9EntityEventSuggestionCategory>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/suggestion-categories/stream',
        });
    }

    /**
     * @param id 
     * @returns SuggestionCategory OK
     * @throws ApiError
     */
    public getSuggestionCategories1(
id: number,
): CancelablePromise<SuggestionCategory> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/suggestion-categories/{id}',
            path: {
                'id': id,
            },
        });
    }

    /**
     * @param id 
     * @param requestBody 
     * @returns SuggestionCategory OK
     * @throws ApiError
     */
    public putSuggestionCategories(
id: number,
requestBody?: SuggestionCategoryDTO,
): CancelablePromise<SuggestionCategory> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/suggestion-categories/{id}',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @param id 
     * @returns SuggestionCategory OK
     * @throws ApiError
     */
    public deleteSuggestionCategories(
id: number,
): CancelablePromise<SuggestionCategory> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/suggestion-categories/{id}',
            path: {
                'id': id,
            },
        });
    }

    /**
     * @param id 
     * @param requestBody 
     * @returns SuggestionCategory OK
     * @throws ApiError
     */
    public patchSuggestionCategories(
id: number,
requestBody?: SuggestionCategoryDTO,
): CancelablePromise<SuggestionCategory> {
        return this.httpRequest.request({
            method: 'PATCH',
            url: '/suggestion-categories/{id}',
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
     * @returns PageDocTypeField OK
     * @throws ApiError
     */
    public getSuggestionCategoriesDocTypeFields(
id: number,
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageDocTypeField> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/suggestion-categories/{id}/doc-type-fields',
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
     * @param docTypeFieldId 
     * @param id 
     * @returns Tuple2SuggestionCategoryDocTypeField OK
     * @throws ApiError
     */
    public putSuggestionCategoriesDocTypeFields(
docTypeFieldId: number,
id: number,
): CancelablePromise<Tuple2SuggestionCategoryDocTypeField> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/suggestion-categories/{id}/doc-type-fields/{docTypeFieldId}',
            path: {
                'docTypeFieldId': docTypeFieldId,
                'id': id,
            },
        });
    }

    /**
     * @param docTypeFieldId 
     * @param id 
     * @returns Tuple2SuggestionCategoryDocTypeField OK
     * @throws ApiError
     */
    public deleteSuggestionCategoriesDocTypeFields(
docTypeFieldId: number,
id: number,
): CancelablePromise<Tuple2SuggestionCategoryDocTypeField> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/suggestion-categories/{id}/doc-type-fields/{docTypeFieldId}',
            path: {
                'docTypeFieldId': docTypeFieldId,
                'id': id,
            },
        });
    }

}
