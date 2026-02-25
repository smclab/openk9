/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { TriggerResponse } from '../models/TriggerResponse';
import type { TriggerV2ResourceDTO } from '../models/TriggerV2ResourceDTO';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class TriggerWithDateResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * Trigger
     * @param requestBody 
     * @returns TriggerResponse Auto Generate successful
     * @throws ApiError
     */
    public postApiDatasourceV2Trigger(
requestBody: TriggerV2ResourceDTO,
): CancelablePromise<TriggerResponse> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/v2/trigger',
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
