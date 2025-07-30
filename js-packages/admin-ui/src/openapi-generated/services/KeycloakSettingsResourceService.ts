/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { KeycloakSettings } from '../models/KeycloakSettings';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class KeycloakSettingsResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * Keycloak Settings
     * @returns KeycloakSettings OK
     * @throws ApiError
     */
    public getApiDatasourceOauth2Settings(): CancelablePromise<KeycloakSettings> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/oauth2/settings',
        });
    }

    /**
     * Settings Js
     * @returns string OK
     * @throws ApiError
     */
    public getApiDatasourceOauth2SettingsJs(): CancelablePromise<string> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/oauth2/settings.js',
        });
    }

}
