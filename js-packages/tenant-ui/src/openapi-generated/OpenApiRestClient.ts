/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseHttpRequest } from './core/BaseHttpRequest';
import type { OpenAPIConfig } from './core/OpenAPI';
import { FetchHttpRequest } from './core/FetchHttpRequest';

import { TenantManagementService } from './services/TenantManagementService';
import { TenantProvisioningService } from './services/TenantProvisioningService';

type HttpRequestConstructor = new (config: OpenAPIConfig) => BaseHttpRequest;

export class OpenApiRestClient {

    public readonly tenantManagement: TenantManagementService;
    public readonly tenantProvisioning: TenantProvisioningService;

    public readonly request: BaseHttpRequest;

    constructor(config?: Partial<OpenAPIConfig>, HttpRequest: HttpRequestConstructor = FetchHttpRequest) {
        this.request = new HttpRequest({
            BASE: config?.BASE ?? '',
            VERSION: config?.VERSION ?? '2026.1.0-SNAPSHOT',
            WITH_CREDENTIALS: config?.WITH_CREDENTIALS ?? false,
            CREDENTIALS: config?.CREDENTIALS ?? 'include',
            TOKEN: config?.TOKEN,
            USERNAME: config?.USERNAME,
            PASSWORD: config?.PASSWORD,
            HEADERS: config?.HEADERS,
            ENCODE_PATH: config?.ENCODE_PATH,
        });

        this.tenantManagement = new TenantManagementService(this.request);
        this.tenantProvisioning = new TenantProvisioningService(this.request);
    }
}

