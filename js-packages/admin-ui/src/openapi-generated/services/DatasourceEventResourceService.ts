/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { DatasourceEvent } from '../models/DatasourceEvent';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class DatasourceEventResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @returns DatasourceEvent OK
     * @throws ApiError
     */
    public getApiDatasourceDatasourceEvent(): CancelablePromise<Array<DatasourceEvent>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/datasource-event',
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @param datasourceId 
     * @returns DatasourceEvent OK
     * @throws ApiError
     */
    public getApiDatasourceDatasourceEvent1(
datasourceId: number,
): CancelablePromise<Array<DatasourceEvent>> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/datasource-event/{datasourceId}',
            path: {
                'datasourceId': datasourceId,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

}
