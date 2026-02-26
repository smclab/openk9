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
import type { DocTypeTemplate } from '../models/DocTypeTemplate';
import type { DocTypeTemplateDTO } from '../models/DocTypeTemplateDTO';
import type { K9Column } from '../models/K9Column';
import type { K9EntityEventDocTypeTemplate } from '../models/K9EntityEventDocTypeTemplate';
import type { PageDocTypeTemplate } from '../models/PageDocTypeTemplate';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class TemplateResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * Persist
     * @param requestBody 
     * @returns DocTypeTemplate OK
     * @throws ApiError
     */
    public postApiDatasourceTemplates(
requestBody: DocTypeTemplateDTO,
): CancelablePromise<DocTypeTemplate> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/templates',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Bad Request`,
            },
        });
    }

    /**
     * Find All
     * @param afterId 
     * @param beforeId 
     * @param limit 
     * @param searchText 
     * @param sortBy 
     * @returns PageDocTypeTemplate OK
     * @throws ApiError
     */
    public getApiDatasourceTemplates(
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageDocTypeTemplate> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/templates',
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
     * Count
     * @returns number OK
     * @throws ApiError
     */
    public getApiDatasourceTemplatesCount(): CancelablePromise<number> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/templates/count',
        });
    }

    /**
     * Get Processor
     * @returns K9EntityEventDocTypeTemplate OK
     * @throws ApiError
     */
    public getApiDatasourceTemplatesStream(): CancelablePromise<Array<K9EntityEventDocTypeTemplate>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/templates/stream',
        });
    }

    /**
     * Update
     * @param id 
     * @param requestBody 
     * @returns DocTypeTemplate OK
     * @throws ApiError
     */
    public putApiDatasourceTemplates(
id: number,
requestBody: DocTypeTemplateDTO,
): CancelablePromise<DocTypeTemplate> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/api/datasource/templates/{id}',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Bad Request`,
            },
        });
    }

    /**
     * Patch
     * @param id 
     * @param requestBody 
     * @returns DocTypeTemplate OK
     * @throws ApiError
     */
    public patchApiDatasourceTemplates(
id: number,
requestBody: DocTypeTemplateDTO,
): CancelablePromise<DocTypeTemplate> {
        return this.httpRequest.request({
            method: 'PATCH',
            url: '/api/datasource/templates/{id}',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * Find By Id
     * @param id 
     * @returns DocTypeTemplate OK
     * @throws ApiError
     */
    public getApiDatasourceTemplates1(
id: number,
): CancelablePromise<DocTypeTemplate> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/templates/{id}',
            path: {
                'id': id,
            },
        });
    }

    /**
     * Delete By Id
     * @param id 
     * @returns DocTypeTemplate OK
     * @throws ApiError
     */
    public deleteApiDatasourceTemplates(
id: number,
): CancelablePromise<DocTypeTemplate> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/api/datasource/templates/{id}',
            path: {
                'id': id,
            },
        });
    }

    /**
     * Get Template Compiled
     * @param id 
     * @returns string OK
     * @throws ApiError
     */
    public getApiDatasourceTemplatesCompiled(
id: number,
): CancelablePromise<string> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/templates/{id}/compiled',
            path: {
                'id': id,
            },
        });
    }

}

