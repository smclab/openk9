/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { PluginDriverDocTypesDTO } from '../models/PluginDriverDocTypesDTO';
import type { PluginDriverDTO } from '../models/PluginDriverDTO';
import type { PluginDriverFormDTO } from '../models/PluginDriverFormDTO';
import type { PluginDriverHealthDTO } from '../models/PluginDriverHealthDTO';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class PluginDriverResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * Get Doc Types
     * @param id 
     * @returns PluginDriverDocTypesDTO OK
     * @throws ApiError
     */
    public getApiDatasourcePluginDriversDocumentTypes(
id: number,
): CancelablePromise<PluginDriverDocTypesDTO> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/pluginDrivers/documentTypes/{id}',
            path: {
                'id': id,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * Get Form
     * @param requestBody 
     * @returns PluginDriverFormDTO OK
     * @throws ApiError
     */
    public postApiDatasourcePluginDriversForm(
requestBody: PluginDriverDTO,
): CancelablePromise<PluginDriverFormDTO> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/pluginDrivers/form',
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
     * Get Form
     * @param id 
     * @returns PluginDriverFormDTO OK
     * @throws ApiError
     */
    public getApiDatasourcePluginDriversForm(
id: number,
): CancelablePromise<PluginDriverFormDTO> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/pluginDrivers/form/{id}',
            path: {
                'id': id,
            },
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * Get Health
     * @param requestBody 
     * @returns PluginDriverHealthDTO OK
     * @throws ApiError
     */
    public postApiDatasourcePluginDriversHealth(
requestBody: PluginDriverDTO,
): CancelablePromise<PluginDriverHealthDTO> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/pluginDrivers/health',
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
     * Get Health
     * @param id 
     * @returns PluginDriverHealthDTO OK
     * @throws ApiError
     */
    public getApiDatasourcePluginDriversHealth(
id: number,
): CancelablePromise<PluginDriverHealthDTO> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/pluginDrivers/health/{id}',
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
