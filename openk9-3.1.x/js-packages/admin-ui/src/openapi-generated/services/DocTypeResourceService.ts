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
     * @deprecated
     * Persist
     * @param requestBody 
     * @returns DocType OK
     * @throws ApiError
     */
    public postApiDatasourceDocTypes(
requestBody: DocTypeDTO,
): CancelablePromise<DocType> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/doc-types',
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
     * @returns PageDocType OK
     * @throws ApiError
     */
    public getApiDatasourceDocTypes(
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageDocType> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/doc-types',
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
    public getApiDatasourceDocTypesCount(): CancelablePromise<number> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/doc-types/count',
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @deprecated
     * Get Processor
     * @returns K9EntityEventDocType OK
     * @throws ApiError
     */
    public getApiDatasourceDocTypesStream(): CancelablePromise<Array<K9EntityEventDocType>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/doc-types/stream',
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
     * @returns DocType OK
     * @throws ApiError
     */
    public putApiDatasourceDocTypes(
id: number,
requestBody: DocTypeDTO,
): CancelablePromise<DocType> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/api/datasource/doc-types/{id}',
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
     * @returns DocType OK
     * @throws ApiError
     */
    public patchApiDatasourceDocTypes(
id: number,
requestBody: DocTypeDTO,
): CancelablePromise<DocType> {
        return this.httpRequest.request({
            method: 'PATCH',
            url: '/api/datasource/doc-types/{id}',
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
     * @returns DocType OK
     * @throws ApiError
     */
    public getApiDatasourceDocTypes1(
id: number,
): CancelablePromise<DocType> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/doc-types/{id}',
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
     * @returns DocType OK
     * @throws ApiError
     */
    public deleteApiDatasourceDocTypes(
id: number,
): CancelablePromise<DocType> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/api/datasource/doc-types/{id}',
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
    public getApiDatasourceDocTypesDocTypeFields(
id: number,
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageDocTypeField> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/doc-types/{id}/doc-type-fields',
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
     * Add Doc Type Field
     * @param id 
     * @param requestBody 
     * @returns Tuple2DocTypeDocTypeField OK
     * @throws ApiError
     */
    public putApiDatasourceDocTypesDocTypeFields(
id: number,
requestBody: DocTypeFieldDTO,
): CancelablePromise<Tuple2DocTypeDocTypeField> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/api/datasource/doc-types/{id}/doc-type-fields',
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
     * Remove Doc Type Field
     * @param docTypeFieldId 
     * @param id 
     * @returns Tuple2DocTypeLong OK
     * @throws ApiError
     */
    public deleteApiDatasourceDocTypesDocTypeFields(
docTypeFieldId: number,
id: number,
): CancelablePromise<Tuple2DocTypeLong> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/api/datasource/doc-types/{id}/doc-type-fields/{docTypeFieldId}',
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

