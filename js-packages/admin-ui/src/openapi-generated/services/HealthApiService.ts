/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ResourceUri } from '../models/ResourceUri';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class HealthApiService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * Get Health
     * @param requestBody 
     * @returns any Health Check Ok
     * @throws ApiError
     */
    public health(
requestBody: ResourceUri,
): CancelablePromise<any> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/enrichers/health',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Bad Request`,
                404: `Not found`,
                500: `Internal Server Error`,
            },
        });
    }

}
