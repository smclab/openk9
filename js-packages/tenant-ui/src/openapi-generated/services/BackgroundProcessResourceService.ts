/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BackgroundProcess } from '../models/BackgroundProcess';
import type { Status } from '../models/Status';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class BackgroundProcessResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @returns BackgroundProcess OK
     * @throws ApiError
     */
    public getApiTenantManagerTenantManagerBackgroundProcessAll(): CancelablePromise<Array<BackgroundProcess>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/tenant-manager/tenant-manager/background-process/all',
        });
    }

    /**
     * @param id 
     * @returns BackgroundProcess OK
     * @throws ApiError
     */
    public getApiTenantManagerTenantManagerBackgroundProcessId(
id: number,
): CancelablePromise<BackgroundProcess> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/tenant-manager/tenant-manager/background-process/id/{id}',
            path: {
                'id': id,
            },
        });
    }

    /**
     * @param status 
     * @returns BackgroundProcess OK
     * @throws ApiError
     */
    public getApiTenantManagerTenantManagerBackgroundProcessStatus(
status: Status,
): CancelablePromise<Array<BackgroundProcess>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/tenant-manager/tenant-manager/background-process/status/{status}',
            path: {
                'status': status,
            },
        });
    }

}
