/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseHttpRequest } from './core/BaseHttpRequest';
import type { OpenAPIConfig } from './core/OpenAPI';
import { FetchHttpRequest } from './core/FetchHttpRequest';

import { BackgroundProcessResourceService } from './services/BackgroundProcessResourceService';
import { MetricsResourceService } from './services/MetricsResourceService';
import { OAuth2SettingsResourceService } from './services/OAuth2SettingsResourceService';
import { TenantManagerResourceService } from './services/TenantManagerResourceService';

type HttpRequestConstructor = new (config: OpenAPIConfig) => BaseHttpRequest;

export class OpenApiRestClient {

    public readonly backgroundProcessResource: BackgroundProcessResourceService;
    public readonly metricsResource: MetricsResourceService;
    public readonly oAuth2SettingsResource: OAuth2SettingsResourceService;
    public readonly tenantManagerResource: TenantManagerResourceService;

    public readonly request: BaseHttpRequest;

    constructor(config?: Partial<OpenAPIConfig>, HttpRequest: HttpRequestConstructor = FetchHttpRequest) {
        this.request = new HttpRequest({
            BASE: config?.BASE ?? '',
            VERSION: config?.VERSION ?? '1.4.0-SNAPSHOT',
            WITH_CREDENTIALS: config?.WITH_CREDENTIALS ?? false,
            CREDENTIALS: config?.CREDENTIALS ?? 'include',
            TOKEN: config?.TOKEN,
            USERNAME: config?.USERNAME,
            PASSWORD: config?.PASSWORD,
            HEADERS: config?.HEADERS,
            ENCODE_PATH: config?.ENCODE_PATH,
        });

        this.backgroundProcessResource = new BackgroundProcessResourceService(this.request);
        this.metricsResource = new MetricsResourceService(this.request);
        this.oAuth2SettingsResource = new OAuth2SettingsResourceService(this.request);
        this.tenantManagerResource = new TenantManagerResourceService(this.request);
    }
}
