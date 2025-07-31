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
