/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class SchedulerResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @param schedulerId 
     * @returns string OK
     * @throws ApiError
     */
    public getApiDatasourceSchedulers(
schedulerId: number,
): CancelablePromise<Array<string>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/schedulers/{schedulerId}',
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
