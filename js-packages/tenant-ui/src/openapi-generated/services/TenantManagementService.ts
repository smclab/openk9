/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CreateTablesResponse } from '../models/CreateTablesResponse';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class TenantManagementService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * Populate the database schema of an existing tenant
     * Creates the application tables for the tenant identified by the path parameter. The tenant's database schema must already exist.
     * @param id Identifier of the tenant whose tables must be created.
     * @returns CreateTablesResponse Tables created
     * @throws ApiError
     */
    public createTables(
        id: number,
    ): CancelablePromise<CreateTablesResponse> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/tenant-manager/tenant/{id}/tables',
            path: {
                'id': id,
            },
        });
    }

}
