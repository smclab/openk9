/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { DatasourceJobStatus } from '../models/DatasourceJobStatus';
import type { TriggerV2ResourceDTO } from '../models/TriggerV2ResourceDTO';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class TriggerWithDateResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * Trigger
     * @param requestBody 
     * @returns DatasourceJobStatus OK
     * @throws ApiError
     */
    public postApiDatasourceV2Trigger(
requestBody: TriggerV2ResourceDTO,
): CancelablePromise<DatasourceJobStatus> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/v2/trigger',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Bad Request`,
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

}
