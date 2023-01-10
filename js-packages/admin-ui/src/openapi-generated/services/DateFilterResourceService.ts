/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { DateFilterResponseDto } from '../models/DateFilterResponseDto';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class DateFilterResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @returns DateFilterResponseDto OK
     * @throws ApiError
     */
    public getApiDatasourceV1DateFilter(): CancelablePromise<Array<DateFilterResponseDto>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/v1/date-filter',
        });
    }

}
