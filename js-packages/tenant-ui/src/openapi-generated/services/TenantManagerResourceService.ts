/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CreateTenantRequest } from '../models/CreateTenantRequest';
import type { DeleteTenantRequest } from '../models/DeleteTenantRequest';
import type { DeleteTenantResponse } from '../models/DeleteTenantResponse';
import type { EffectiveDeleteTenantRequest } from '../models/EffectiveDeleteTenantRequest';
import type { RequestId } from '../models/RequestId';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class TenantManagerResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @param requestBody 
     * @returns RequestId OK
     * @throws ApiError
     */
    public postTenantManagerTenant(
requestBody?: CreateTenantRequest,
): CancelablePromise<RequestId> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/tenant-manager/tenant',
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @param requestBody 
     * @returns DeleteTenantResponse OK
     * @throws ApiError
     */
    public postTenantManagerTenantDelete(
requestBody?: DeleteTenantRequest,
): CancelablePromise<DeleteTenantResponse> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/tenant-manager/tenant/delete',
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * @param requestBody 
     * @returns DeleteTenantResponse OK
     * @throws ApiError
     */
    public deleteTenantManagerTenantDelete(
requestBody?: EffectiveDeleteTenantRequest,
): CancelablePromise<DeleteTenantResponse> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/tenant-manager/tenant/delete',
            body: requestBody,
            mediaType: 'application/json',
        });
    }

}
