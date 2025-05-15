/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class SchedulerResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
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
