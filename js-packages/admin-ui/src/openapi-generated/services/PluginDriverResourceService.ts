/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { DocType } from '../models/DocType';
import type { FormTemplate } from '../models/FormTemplate';
import type { HealthDTO } from '../models/HealthDTO';
import type { PluginDriverDocTypesDTO } from '../models/PluginDriverDocTypesDTO';
import type { PluginDriverDTO } from '../models/PluginDriverDTO';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class PluginDriverResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * Get Doc Types
     * @param id Plugin Driver's id
     * @returns PluginDriverDocTypesDTO The list of the Document Types created from this Plugin Driver
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
                400: `Bad Request`,
                404: `Not found`,
                500: `Internal Server Error`,
            },
        });
    }

    /**
     * Create Doc Types
     * @param id Plugin Driver's id
     * @returns DocType OK
     * @throws ApiError
     */
    public postApiDatasourcePluginDriversDocumentTypes(
id: number,
): CancelablePromise<Array<DocType>> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/pluginDrivers/documentTypes/{id}',
            path: {
                'id': id,
            },
        });
    }

    /**
     * Get Form
     * @param id Plugin Driver's id
     * @returns FormTemplate Form returned
     * @throws ApiError
     */
    public getApiDatasourcePluginDriversForm(
id: number,
): CancelablePromise<FormTemplate> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/pluginDrivers/form/{id}',
            path: {
                'id': id,
            },
            errors: {
                400: `Bad Request`,
                404: `Not found`,
                500: `Internal Server Error`,
            },
        });
    }

    /**
     * Get Health
     * @param requestBody 
     * @returns HealthDTO Plugin Driver Health Status
     * @throws ApiError
     */
    public postApiDatasourcePluginDriversHealth(
requestBody: PluginDriverDTO,
): CancelablePromise<HealthDTO> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/pluginDrivers/health',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Bad Request`,
                404: `Not found`,
                500: `Internal Server Error`,
            },
        });
    }

    /**
     * Get Health
     * @param id Plugin Driver's id
     * @returns HealthDTO Plugin Driver Health Status
     * @throws ApiError
     */
    public getApiDatasourcePluginDriversHealth(
id: number,
): CancelablePromise<HealthDTO> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/pluginDrivers/health/{id}',
            path: {
                'id': id,
            },
            errors: {
                400: `Bad Request`,
                404: `Not found`,
                500: `Internal Server Error`,
            },
        });
    }

}
