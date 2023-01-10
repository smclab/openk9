/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { Settings } from '../models/Settings';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class OAuth2SettingsResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @returns Settings OK
     * @throws ApiError
     */
    public getApiDatasourceOauth2Settings(): CancelablePromise<Settings> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/datasource/oauth2/settings',
        });
    }

    /**
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
