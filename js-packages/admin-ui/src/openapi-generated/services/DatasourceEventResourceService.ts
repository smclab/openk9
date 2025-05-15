/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { DatasourceMessage } from '../models/DatasourceMessage';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class DatasourceEventResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @returns DatasourceMessage OK
     * @throws ApiError
     */
    public getApiDatasourceDatasourceEvent(): CancelablePromise<Array<DatasourceMessage>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/datasource-event',
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

}
