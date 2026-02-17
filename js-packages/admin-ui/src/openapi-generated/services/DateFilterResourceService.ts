/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { DocTypeFieldResponseDTO } from '../models/DocTypeFieldResponseDTO';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class DateFilterResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @deprecated
     * Get Fields
     * @param translated 
     * @returns DocTypeFieldResponseDTO OK
     * @throws ApiError
     */
    public getApiDatasourceV1DateFilter(
translated: boolean = false,
): CancelablePromise<Array<DocTypeFieldResponseDTO>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/v1/date-filter',
            query: {
                'translated': translated,
            },
        });
    }

}
