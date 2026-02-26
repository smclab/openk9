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
import type { StatusResponse } from '../models/StatusResponse';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class SchedulerResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * Status
     * @returns StatusResponse OK
     * @throws ApiError
     */
    public getApiDatasourceSchedulersStatus(): CancelablePromise<StatusResponse> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/schedulers/status',
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * Cancel Scheduling
     * @param schedulerId 
     * @returns any Created
     * @throws ApiError
     */
    public postApiDatasourceSchedulersCancelScheduling(
schedulerId: number,
): CancelablePromise<any> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/schedulers/{schedulerId}/cancelScheduling',
            path: {
                'schedulerId': schedulerId,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * Close Scheduling
     * @param schedulerId 
     * @returns any Created
     * @throws ApiError
     */
    public postApiDatasourceSchedulersCloseScheduling(
schedulerId: number,
): CancelablePromise<any> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/schedulers/{schedulerId}/closeScheduling',
            path: {
                'schedulerId': schedulerId,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * Get Deleted Content Ids
     * @param schedulerId 
     * @returns string OK
     * @throws ApiError
     */
    public getApiDatasourceSchedulersGetDeletedContentIds(
schedulerId: number,
): CancelablePromise<Array<string>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/schedulers/{schedulerId}/getDeletedContentIds',
            path: {
                'schedulerId': schedulerId,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * Reroute Scheduling
     * @param schedulerId 
     * @returns any Created
     * @throws ApiError
     */
    public postApiDatasourceSchedulersRerouteScheduling(
schedulerId: number,
): CancelablePromise<any> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/schedulers/{schedulerId}/rerouteScheduling',
            path: {
                'schedulerId': schedulerId,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

}

