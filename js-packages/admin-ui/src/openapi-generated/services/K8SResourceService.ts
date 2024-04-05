/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { PodMetricResponse } from '../models/PodMetricResponse';
import type { PodResponse } from '../models/PodResponse';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class K8SResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @param namespace
     * @param podName
     * @returns PodMetricResponse OK
     * @throws ApiError
     */
    public getApiK8SClientK8SGetMetrics(
        namespace: string,
        podName: string,
    ): CancelablePromise<PodMetricResponse> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/k8s-client/k8s/get/metrics/{namespace}/{podName}',
            path: {
                'namespace': namespace,
                'podName': podName,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @returns PodResponse OK
     * @throws ApiError
     */
    public getApiK8SClientK8SGetPods(): CancelablePromise<Array<PodResponse>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/k8s-client/k8s/get/pods',
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @param podName
     * @param tail
     * @returns void
     * @throws ApiError
     */
    public getApiK8SClientK8SLogSse(
        podName: string,
        tail: number = 1000,
    ): CancelablePromise<void> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/k8s-client/k8s/log/sse/{podName}',
            path: {
                'podName': podName,
            },
            query: {
                'tail': tail,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @param namespace
     * @param podName
     * @param tail
     * @returns any OK
     * @throws ApiError
     */
    public getApiK8SClientK8SLog(
        namespace: string,
        podName: string,
        tail: number = -1,
    ): CancelablePromise<any> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/k8s-client/k8s/log/{namespace}/{podName}',
            path: {
                'namespace': namespace,
                'podName': podName,
            },
            query: {
                'tail': tail,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @returns PodResponse OK
     * @throws ApiError
     */
    public getApiK8SClientK8SPodsSse(): CancelablePromise<Array<PodResponse>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/k8s-client/k8s/pods/sse',
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

}
