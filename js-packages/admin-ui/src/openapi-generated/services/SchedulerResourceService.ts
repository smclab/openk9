/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class SchedulerResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * Status
     * @returns any Health statud for each scheduled Datasource Indexing Job.
     * @throws ApiError
     */
    public getApiDatasourceSchedulersStatus(): CancelablePromise<any> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/schedulers/status',
            errors: {
                400: `Bad Request`,
                404: `Not found`,
                500: `Internal Server Error`,
            },
        });
    }

    /**
     * Cancel Scheduling
     * @param schedulerId Scheduler entity ID
     * @returns void 
     * @throws ApiError
     */
    public postApiDatasourceSchedulersCancelScheduling(
schedulerId: number,
): CancelablePromise<void> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/schedulers/{schedulerId}/cancelScheduling',
            path: {
                'schedulerId': schedulerId,
            },
            errors: {
                400: `Bad Request`,
                404: `Not found`,
                500: `Internal Server Error`,
            },
        });
    }

    /**
     * Close Scheduling
     * @param schedulerId Scheduler entity ID
     * @returns void 
     * @throws ApiError
     */
    public postApiDatasourceSchedulersCloseScheduling(
schedulerId: number,
): CancelablePromise<void> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/schedulers/{schedulerId}/closeScheduling',
            path: {
                'schedulerId': schedulerId,
            },
            errors: {
                400: `Bad Request`,
                404: `Not found`,
                500: `Internal Server Error`,
            },
        });
    }

    /**
     * Get Deleted Content Ids
     * @param schedulerId 
     * @returns any List of deleted content ids returned
     * @throws ApiError
     */
    public getApiDatasourceSchedulersGetDeletedContentIds(
schedulerId: number,
): CancelablePromise<Array<any>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/schedulers/{schedulerId}/getDeletedContentIds',
            path: {
                'schedulerId': schedulerId,
            },
            errors: {
                400: `Bad Request`,
                404: `Not found`,
                500: `Internal Server Error`,
            },
        });
    }

    /**
     * Reroute Scheduling
     * @param schedulerId Scheduler entity ID
     * @returns void 
     * @throws ApiError
     */
    public postApiDatasourceSchedulersRerouteScheduling(
schedulerId: number,
): CancelablePromise<void> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/schedulers/{schedulerId}/rerouteScheduling',
            path: {
                'schedulerId': schedulerId,
            },
            errors: {
                400: `Bad Request`,
                404: `Not found`,
                500: `Internal Server Error`,
            },
        });
    }

}
