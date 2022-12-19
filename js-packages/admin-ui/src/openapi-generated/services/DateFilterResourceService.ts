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
    public getV1DateFilter(): CancelablePromise<Array<DateFilterResponseDto>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/v1/date-filter',
        });
    }

}
