/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { JsonObject } from '../models/JsonObject';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class PipelineResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @param tokenId 
     * @param requestBody 
     * @returns any Created
     * @throws ApiError
     */
    public postApiDatasourcePipelineCallback(
tokenId: string,
requestBody?: JsonObject,
): CancelablePromise<any> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/pipeline/callback/{token-id}',
            path: {
                'token-id': tokenId,
            },
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @param enrichItemId 
     * @param requestBody 
     * @returns JsonObject OK
     * @throws ApiError
     */
    public postApiDatasourcePipelineEnrichItem(
enrichItemId: number,
requestBody?: JsonObject,
): CancelablePromise<JsonObject> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/pipeline/enrich-item/{enrich-item-id}',
            path: {
                'enrich-item-id': enrichItemId,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

}
