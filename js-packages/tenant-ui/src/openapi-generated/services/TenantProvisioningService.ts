/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CreateConnectorRequest } from '../models/CreateConnectorRequest';
import type { CreateConnectorResponse } from '../models/CreateConnectorResponse';
import type { CreateTenantRequest } from '../models/CreateTenantRequest';
import type { DeleteTenantRequest } from '../models/DeleteTenantRequest';
import type { DeleteTenantResponse } from '../models/DeleteTenantResponse';
import type { EffectiveDeleteTenantRequest } from '../models/EffectiveDeleteTenantRequest';
import type { InitTenantRequest } from '../models/InitTenantRequest';
import type { InitTenantResponse } from '../models/InitTenantResponse';
import type { TenantResponseDTO } from '../models/TenantResponseDTO';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class TenantProvisioningService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * Create a preset connector plugin driver for a tenant
     * Deploys the Helm chart of the selected preset connector for the target tenant and registers the corresponding plugin driver. The preset is selected from the supported values (YOUTUBE, CRAWLER, EMAIL, GITLAB, SITEMAP, DATABASE, MINIO). The saga runs asynchronously and the endpoint always returns HTTP 200: the response's `result` field carries either the saga outcome (SUCCESS, ERROR, COMPENSATION, COMPENSATION_ERROR) or, when the orchestrator itself fails (e.g. ask timeout), the failure message. Callers must inspect `result` to determine the actual outcome.
     * @param requestBody
     * @returns CreateConnectorResponse Connector creation saga completed
     * @throws ApiError
     */
    public createConnector(
        requestBody: CreateConnectorRequest,
    ): CancelablePromise<CreateConnectorResponse> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/provisioning/connector',
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * Initialize the default bucket for an existing tenant
     * Triggers the post-provisioning step that creates the default datasource bucket for an existing tenant schema. Returns the identifier of the bucket created in the datasource service.
     * @param requestBody
     * @returns InitTenantResponse Tenant initialization successful
     * @throws ApiError
     */
    public initTenant(
        requestBody: InitTenantRequest,
    ): CancelablePromise<InitTenantResponse> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/provisioning/initTenant',
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * Create and provision a new tenant
     * Provisions a new tenant: assigns a virtual host, creates the database schema, configures the security model, optionally bootstraps a Keycloak realm, and exposes the configured ingress routes. Returns the descriptor of the created tenant.
     * @param requestBody
     * @returns TenantResponseDTO Tenant created
     * @throws ApiError
     */
    public createTenant(
        requestBody: CreateTenantRequest,
    ): CancelablePromise<TenantResponseDTO> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/tenant-manager/tenant',
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * Request the deletion of a tenant
     * Issues a deletion request for the tenant identified by virtual host. Returns a confirmation token that must be supplied to the DELETE endpoint to actually remove the tenant.
     * @param requestBody
     * @returns DeleteTenantResponse Deletion token issued
     * @throws ApiError
     */
    public requestDeleteTenant(
        requestBody: DeleteTenantRequest,
    ): CancelablePromise<DeleteTenantResponse> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/tenant-manager/tenant/delete',
            body: requestBody,
            mediaType: 'application/json',
        });
    }

    /**
     * Confirm and execute a tenant deletion
     * Permanently removes the tenant identified by virtual host, using the confirmation token previously issued by the POST /delete endpoint.
     * @param requestBody
     * @returns DeleteTenantResponse Tenant deleted
     * @throws ApiError
     */
    public deleteTenant(
        requestBody: EffectiveDeleteTenantRequest,
    ): CancelablePromise<DeleteTenantResponse> {
        return this.httpRequest.request({
            method: 'DELETE',
            url: '/tenant-manager/tenant/delete',
            body: requestBody,
            mediaType: 'application/json',
        });
    }

}
