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
import type { JsonObject } from '../models/JsonObject';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class PipelineResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * Callback
     * @param tokenId 
     * @param requestBody 
     * @returns any Created
     * @throws ApiError
     */
    public postApiDatasourcePipelineCallback(
tokenId: string,
requestBody: JsonObject,
): CancelablePromise<any> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/pipeline/callback/{token-id}',
            path: {
                'token-id': tokenId,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Bad Request`,
            },
        });
    }

    /**
     * @deprecated
     * Call Enrich Item
     * @param enrichItemId 
     * @param requestBody 
     * @returns JsonObject OK
     * @throws ApiError
     */
    public postApiDatasourcePipelineEnrichItem(
enrichItemId: number,
requestBody: JsonObject,
): CancelablePromise<JsonObject> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/pipeline/enrich-item/{enrich-item-id}',
            path: {
                'enrich-item-id': enrichItemId,
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

}

