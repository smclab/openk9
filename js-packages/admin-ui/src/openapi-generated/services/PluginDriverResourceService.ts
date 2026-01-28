/*
* Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { DocType } from '../models/DocType';
import type { FormTemplate } from '../models/FormTemplate';
import type { PluginDriverDocTypesDTO } from '../models/PluginDriverDocTypesDTO';
import type { PluginDriverDTO } from '../models/PluginDriverDTO';
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
     * Create Doc Types
     * @param id 
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
            errors: {
                401: `Not Authorized`,
                403: `Not Allowed`,
            },
        });
    }

    /**
     * Get Form
     * @param requestBody 
     * @returns FormTemplate OK
     * @throws ApiError
     */
    public postApiDatasourcePluginDriversForm(
requestBody: PluginDriverDTO,
): CancelablePromise<FormTemplate> {
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
     * @returns FormTemplate OK
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

