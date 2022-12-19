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
    public getOauth2Settings(): CancelablePromise<Settings> {
        return this.httpRequest.request({
            method: 'GET',
            url: '/oauth2/settings',
        });
    }

}
