/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { MlDto } from '../models/MlDto';
import type { MlPodResponse } from '../models/MlPodResponse';
import type { ModelActionesponse } from '../models/ModelActionesponse';
import type { PodResponse } from '../models/PodResponse';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class Mlk8SResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @param name
     * @returns ModelActionesponse OK
     * @throws ApiError
     */
    public deleteApiK8SClientK8SDeleteMlModel(
        name: string,
    ): CancelablePromise<ModelActionesponse> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/api/k8s-client/k8s/delete-ml-model/{name}',
            path: {
                'name': name,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @param parserName
     * @returns ModelActionesponse OK
     * @throws ApiError
     */
    public deleteApiK8SClientK8SDeleteParserModel(
        parserName: string,
    ): CancelablePromise<ModelActionesponse> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/api/k8s-client/k8s/delete-parser-model/{parserName}',
            path: {
                'parserName': parserName,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @param requestBody
     * @returns ModelActionesponse OK
     * @throws ApiError
     */
    public postApiK8SClientK8SDeployMlModel(
        requestBody?: MlDto,
    ): CancelablePromise<ModelActionesponse> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/k8s-client/k8s/deploy-ml-model',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @param parserName
     * @returns ModelActionesponse OK
     * @throws ApiError
     */
    public postApiK8SClientK8SDeployParser(
        parserName: string,
    ): CancelablePromise<ModelActionesponse> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/k8s-client/k8s/deploy-parser/{parserName}',
            path: {
                'parserName': parserName,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @param parserName
     * @returns PodResponse OK
     * @throws ApiError
     */
    public getApiK8SClientK8SGetPod(
        parserName: string,
    ): CancelablePromise<Array<PodResponse>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/k8s-client/k8s/get/pod/{parserName}',
            path: {
                'parserName': parserName,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @returns MlPodResponse OK
     * @throws ApiError
     */
    public getApiK8SClientK8SGetPodsMl(): CancelablePromise<Array<MlPodResponse>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/k8s-client/k8s/get/pods/ml',
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

}
