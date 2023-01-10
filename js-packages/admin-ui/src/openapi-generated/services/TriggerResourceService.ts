/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { TriggerResourceRequest } from '../models/TriggerResourceRequest';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class TriggerResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @param requestBody 
     * @returns number OK
     * @throws ApiError
     */
    public postApiDatasourceV1Trigger(
requestBody?: TriggerResourceRequest,
): CancelablePromise<Array<number>> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/v1/trigger',
            body: requestBody,
            mediaType: 'application/json',
        });
    }

}
