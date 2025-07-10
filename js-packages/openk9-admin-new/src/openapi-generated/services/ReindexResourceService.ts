/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { DatasourceJobStatus } from '../models/DatasourceJobStatus';
import type { TriggerResourceDTO } from '../models/TriggerResourceDTO';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class ReindexResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @deprecated
     * Reindex
     * @param requestBody 
     * @returns DatasourceJobStatus OK
     * @throws ApiError
     */
    public postApiDatasourceV1IndexReindex(
requestBody: TriggerResourceDTO,
): CancelablePromise<Array<DatasourceJobStatus>> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/v1/index/reindex',
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
