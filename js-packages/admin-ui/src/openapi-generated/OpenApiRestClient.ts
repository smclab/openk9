/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseHttpRequest } from './core/BaseHttpRequest';
import type { OpenAPIConfig } from './core/OpenAPI';
import { FetchHttpRequest } from './core/FetchHttpRequest';

import { BucketResourceService } from './services/BucketResourceService';
import { DataIndexResourceService } from './services/DataIndexResourceService';
import { DatasourceResourceService } from './services/DatasourceResourceService';
import { DateFilterResourceService } from './services/DateFilterResourceService';
import { DocTypeResourceService } from './services/DocTypeResourceService';
import { EnrichItemResourceService } from './services/EnrichItemResourceService';
import { EnrichPipelineResourceService } from './services/EnrichPipelineResourceService';
import { MetricsResourceService } from './services/MetricsResourceService';
import { OAuth2SettingsResourceService } from './services/OAuth2SettingsResourceService';
import { PluginDriverFieldResourceService } from './services/PluginDriverFieldResourceService';
import { ReindexResourceService } from './services/ReindexResourceService';
import { SuggestionCategoryResourceService } from './services/SuggestionCategoryResourceService';
import { TemplateResourceService } from './services/TemplateResourceService';
import { TriggerResourceService } from './services/TriggerResourceService';

type HttpRequestConstructor = new (config: OpenAPIConfig) => BaseHttpRequest;

export class OpenApiRestClient {

    public readonly bucketResource: BucketResourceService;
    public readonly dataIndexResource: DataIndexResourceService;
    public readonly datasourceResource: DatasourceResourceService;
    public readonly dateFilterResource: DateFilterResourceService;
    public readonly docTypeResource: DocTypeResourceService;
    public readonly enrichItemResource: EnrichItemResourceService;
    public readonly enrichPipelineResource: EnrichPipelineResourceService;
    public readonly metricsResource: MetricsResourceService;
    public readonly oAuth2SettingsResource: OAuth2SettingsResourceService;
    public readonly pluginDriverFieldResource: PluginDriverFieldResourceService;
    public readonly reindexResource: ReindexResourceService;
    public readonly suggestionCategoryResource: SuggestionCategoryResourceService;
    public readonly templateResource: TemplateResourceService;
    public readonly triggerResource: TriggerResourceService;

    public readonly request: BaseHttpRequest;

    constructor(config?: Partial<OpenAPIConfig>, HttpRequest: HttpRequestConstructor = FetchHttpRequest) {
        this.request = new HttpRequest({
            BASE: config?.BASE ?? '',
            VERSION: config?.VERSION ?? '1.3.0-SNAPSHOT',
            WITH_CREDENTIALS: config?.WITH_CREDENTIALS ?? false,
            CREDENTIALS: config?.CREDENTIALS ?? 'include',
            TOKEN: config?.TOKEN,
            USERNAME: config?.USERNAME,
            PASSWORD: config?.PASSWORD,
            HEADERS: config?.HEADERS,
            ENCODE_PATH: config?.ENCODE_PATH,
        });

        this.bucketResource = new BucketResourceService(this.request);
        this.dataIndexResource = new DataIndexResourceService(this.request);
        this.datasourceResource = new DatasourceResourceService(this.request);
        this.dateFilterResource = new DateFilterResourceService(this.request);
        this.docTypeResource = new DocTypeResourceService(this.request);
        this.enrichItemResource = new EnrichItemResourceService(this.request);
        this.enrichPipelineResource = new EnrichPipelineResourceService(this.request);
        this.metricsResource = new MetricsResourceService(this.request);
        this.oAuth2SettingsResource = new OAuth2SettingsResourceService(this.request);
        this.pluginDriverFieldResource = new PluginDriverFieldResourceService(this.request);
        this.reindexResource = new ReindexResourceService(this.request);
        this.suggestionCategoryResource = new SuggestionCategoryResourceService(this.request);
        this.templateResource = new TemplateResourceService(this.request);
        this.triggerResource = new TriggerResourceService(this.request);
    }
}
