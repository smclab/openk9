/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CreateTablesResponse } from '../models/CreateTablesResponse';
import type { CreateTenantRequest } from '../models/CreateTenantRequest';
import type { DeleteTenantRequest } from '../models/DeleteTenantRequest';
import type { DeleteTenantResponse } from '../models/DeleteTenantResponse';
import type { EffectiveDeleteTenantRequest } from '../models/EffectiveDeleteTenantRequest';
import type { Tenant } from '../models/Tenant';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class TenantManagerResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @param requestBody 
     * @returns Tenant OK
     * @throws ApiError
     */
    public postApiTenantManagerTenantManagerTenant(
requestBody?: CreateTenantRequest,
): CancelablePromise<Tenant> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/tenant-manager/tenant-manager/tenant',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @param requestBody 
     * @returns DeleteTenantResponse OK
     * @throws ApiError
     */
    public postApiTenantManagerTenantManagerTenantDelete(
requestBody?: DeleteTenantRequest,
): CancelablePromise<DeleteTenantResponse> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/tenant-manager/tenant-manager/tenant/delete',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @param requestBody 
     * @returns DeleteTenantResponse OK
     * @throws ApiError
     */
    public deleteApiTenantManagerTenantManagerTenantDelete(
requestBody?: EffectiveDeleteTenantRequest,
): CancelablePromise<DeleteTenantResponse> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/api/tenant-manager/tenant-manager/tenant/delete',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * @param id 
     * @returns CreateTablesResponse OK
     * @throws ApiError
     */
    public postApiTenantManagerTenantManagerTenantTables(
id: number,
): CancelablePromise<CreateTablesResponse> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/tenant-manager/tenant-manager/tenant/{id}/tables',
            path: {
                'id': id,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

}
