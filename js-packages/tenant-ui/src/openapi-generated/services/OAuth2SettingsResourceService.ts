/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class OAuth2SettingsResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * @returns string OK
     * @throws ApiError
     */
    public getApiTenantManagerOauth2SettingsJs(): CancelablePromise<string> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/api/tenant-manager/oauth2/settings.js',
        });
    }

}
