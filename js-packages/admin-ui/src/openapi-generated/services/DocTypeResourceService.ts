/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { DocType } from '../models/DocType';
import type { DocTypeDTO } from '../models/DocTypeDTO';
import type { DocTypeFieldDTO } from '../models/DocTypeFieldDTO';
import type { K9Column } from '../models/K9Column';
import type { K9EntityEventDocType } from '../models/K9EntityEventDocType';
import type { PageDocType } from '../models/PageDocType';
import type { PageDocTypeField } from '../models/PageDocTypeField';
import type { Tuple2DocTypeDocTypeField } from '../models/Tuple2DocTypeDocTypeField';
import type { Tuple2DocTypeLong } from '../models/Tuple2DocTypeLong';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class DocTypeResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @param afterId 
     * @param beforeId 
     * @param limit 
     * @param searchText 
     * @param sortBy 
     * @returns PageDocType OK
     * @throws ApiError
     */
    public getDocTypes(
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageDocType> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/doc-types',
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
     * @returns DocType OK
     * @throws ApiError
     */
    public postDocTypes(
requestBody?: DocTypeDTO,
): CancelablePromise<DocType> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/doc-types',
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @returns number OK
     * @throws ApiError
     */
    public getDocTypesCount(): CancelablePromise<number> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/doc-types/count',
        });
    }

    /**
     * @returns K9EntityEventDocType OK
     * @throws ApiError
     */
    public getDocTypesStream(): CancelablePromise<Array<K9EntityEventDocType>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/doc-types/stream',
        });
    }

    /**
     * @param id 
     * @returns DocType OK
     * @throws ApiError
     */
    public getDocTypes1(
id: number,
): CancelablePromise<DocType> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/doc-types/{id}',
            path: {
                'id': id,
            },
        });
    }

    /**
     * @param id 
     * @param requestBody 
     * @returns DocType OK
     * @throws ApiError
     */
    public putDocTypes(
id: number,
requestBody?: DocTypeDTO,
): CancelablePromise<DocType> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/doc-types/{id}',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @param id 
     * @returns DocType OK
     * @throws ApiError
     */
    public deleteDocTypes(
id: number,
): CancelablePromise<DocType> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/doc-types/{id}',
            path: {
                'id': id,
            },
        });
    }

    /**
     * @param id 
     * @param requestBody 
     * @returns DocType OK
     * @throws ApiError
     */
    public patchDocTypes(
id: number,
requestBody?: DocTypeDTO,
): CancelablePromise<DocType> {
        return this.httpRequest.request({
            method: 'PATCH',
            url: '/doc-types/{id}',
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
    public getDocTypesDocTypeFields(
id: number,
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageDocTypeField> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/doc-types/{id}/doc-type-fields',
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
     * @param id 
     * @param requestBody 
     * @returns Tuple2DocTypeDocTypeField OK
     * @throws ApiError
     */
    public putDocTypesDocTypeFields(
id: number,
requestBody?: DocTypeFieldDTO,
): CancelablePromise<Tuple2DocTypeDocTypeField> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/doc-types/{id}/doc-type-fields',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @param docTypeFieldId 
     * @param id 
     * @returns Tuple2DocTypeLong OK
     * @throws ApiError
     */
    public deleteDocTypesDocTypeFields(
docTypeFieldId: number,
id: number,
): CancelablePromise<Tuple2DocTypeLong> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/doc-types/{id}/doc-type-fields/{docTypeFieldId}',
            path: {
                'docTypeFieldId': docTypeFieldId,
                'id': id,
            },
        });
    }

}
