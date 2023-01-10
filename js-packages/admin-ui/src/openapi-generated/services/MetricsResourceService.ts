/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class MetricsResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @returns string OK
     * @throws ApiError
     */
    public getApiDatasourceMetricsThreads(): CancelablePromise<string> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/metrics/threads',
        });
    }

}
