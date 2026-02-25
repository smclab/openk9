/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { DatasourceJobStatus } from '../models/DatasourceJobStatus';
import type { TriggerResourceDTO } from '../models/TriggerResourceDTO';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class TriggerResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @deprecated
     * Trigger
     * @param requestBody 
     * @returns DatasourceJobStatus OK
     * @throws ApiError
     */
    public postApiDatasourceV1Trigger(
requestBody: TriggerResourceDTO,
): CancelablePromise<Array<DatasourceJobStatus>> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/v1/trigger',
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
