/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { PartialDocTypeFieldDTO } from '../models/PartialDocTypeFieldDTO';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class DateFilterResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @returns PartialDocTypeFieldDTO OK
     * @throws ApiError
     */
    public getApiDatasourceV1DateFilter(): CancelablePromise<Array<PartialDocTypeFieldDTO>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/v1/date-filter',
        });
    }

}
