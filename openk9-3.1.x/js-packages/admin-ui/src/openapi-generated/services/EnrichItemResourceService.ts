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
     * @deprecated
     * Persist
     * @param requestBody 
     * @returns EnrichItem OK
     * @throws ApiError
     */
    public postApiDatasourceEnrichItems(
requestBody: EnrichItemDTO,
): CancelablePromise<EnrichItem> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/enrich-items',
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
     * @returns PageEnrichItem OK
     * @throws ApiError
     */
    public getApiDatasourceEnrichItems(
afterId: number = -1,
beforeId: number = -1,
limit: number = 20,
searchText?: string,
sortBy?: K9Column,
): CancelablePromise<PageEnrichItem> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/enrich-items',
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
    public getApiDatasourceEnrichItemsCount(): CancelablePromise<number> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/enrich-items/count',
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @deprecated
     * Get Processor
     * @returns K9EntityEventEnrichItem OK
     * @throws ApiError
     */
    public getApiDatasourceEnrichItemsStream(): CancelablePromise<Array<K9EntityEventEnrichItem>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/enrich-items/stream',
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
     * @returns EnrichItem OK
     * @throws ApiError
     */
    public putApiDatasourceEnrichItems(
id: number,
requestBody: EnrichItemDTO,
): CancelablePromise<EnrichItem> {
        return this.httpRequest.request({
            method: 'PUT',
            url: '/api/datasource/enrich-items/{id}',
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
     * @returns EnrichItem OK
     * @throws ApiError
     */
    public patchApiDatasourceEnrichItems(
id: number,
requestBody: EnrichItemDTO,
): CancelablePromise<EnrichItem> {
        return this.httpRequest.request({
            method: 'PATCH',
            url: '/api/datasource/enrich-items/{id}',
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
     * @returns EnrichItem OK
     * @throws ApiError
     */
    public getApiDatasourceEnrichItems1(
id: number,
): CancelablePromise<EnrichItem> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/enrich-items/{id}',
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
     * @returns EnrichItem OK
     * @throws ApiError
     */
    public deleteApiDatasourceEnrichItems(
id: number,
): CancelablePromise<EnrichItem> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/api/datasource/enrich-items/{id}',
            path: {
                'id': id,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

}

