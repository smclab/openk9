/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { PluginDriverDTO } from '../models/PluginDriverDTO';
import type { PluginDriverFormDTO } from '../models/PluginDriverFormDTO';
import type { PluginDriverHealthDTO } from '../models/PluginDriverHealthDTO';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class PluginDriverResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @param requestBody 
     * @returns PluginDriverFormDTO OK
     * @throws ApiError
     */
    public postApiDatasourcePluginDriversForm(
requestBody?: PluginDriverDTO,
): CancelablePromise<PluginDriverFormDTO> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/pluginDrivers/form',
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
     * @param requestBody 
     * @returns PluginDriverHealthDTO OK
     * @throws ApiError
     */
    public postApiDatasourcePluginDriversHealth(
requestBody?: PluginDriverDTO,
): CancelablePromise<PluginDriverHealthDTO> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/pluginDrivers/health',
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
