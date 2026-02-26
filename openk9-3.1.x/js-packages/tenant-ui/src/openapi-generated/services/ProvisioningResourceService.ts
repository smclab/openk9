/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CreateConnectorRequest } from '../models/CreateConnectorRequest';
import type { CreateConnectorResponse } from '../models/CreateConnectorResponse';
import type { InitTenantRequest } from '../models/InitTenantRequest';
import type { InitTenantResponse } from '../models/InitTenantResponse';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class ProvisioningResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * Create Connector
     * @param requestBody 
     * @returns CreateConnectorResponse OK
     * @throws ApiError
     */
    public postApiTenantManagerProvisioningConnector(
requestBody: CreateConnectorRequest,
): CancelablePromise<CreateConnectorResponse> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/tenant-manager/provisioning/connector',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Bad Request`,
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * Init Tenant
     * @param requestBody 
     * @returns InitTenantResponse OK
     * @throws ApiError
     */
    public postApiTenantManagerProvisioningInitTenant(
requestBody: InitTenantRequest,
): CancelablePromise<InitTenantResponse> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/tenant-manager/provisioning/initTenant',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Bad Request`,
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

}
