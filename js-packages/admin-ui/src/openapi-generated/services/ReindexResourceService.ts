/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { ReindexRequestDto } from '../models/ReindexRequestDto';
import type { ReindexResponseDto } from '../models/ReindexResponseDto';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class ReindexResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @param requestBody 
     * @returns ReindexResponseDto OK
     * @throws ApiError
     */
    public postApiDatasourceV1IndexReindex(
requestBody?: ReindexRequestDto,
): CancelablePromise<Array<ReindexResponseDto>> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/v1/index/reindex',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

}
