/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseHttpRequest } from './core/BaseHttpRequest';
import type { OpenAPIConfig } from './core/OpenAPI';
import { FetchHttpRequest } from './core/FetchHttpRequest';

import { BucketResourceService } from './services/BucketResourceService';
import { DataIndexResourceService } from './services/DataIndexResourceService';
import { DatasourceEventResourceService } from './services/DatasourceEventResourceService';
import { DatasourceResourceService } from './services/DatasourceResourceService';
import { DateFilterResourceService } from './services/DateFilterResourceService';
import { DocTypeResourceService } from './services/DocTypeResourceService';
import { EnrichItemResourceService } from './services/EnrichItemResourceService';
import { EnrichPipelineResourceService } from './services/EnrichPipelineResourceService';
import { KeycloakSettingsResourceService } from './services/KeycloakSettingsResourceService';
import { PipelineResourceService } from './services/PipelineResourceService';
import { PluginDriverFieldResourceService } from './services/PluginDriverFieldResourceService';
import { PluginDriverResourceService } from './services/PluginDriverResourceService';
import { ReindexResourceService } from './services/ReindexResourceService';
import { SchedulerResourceService } from './services/SchedulerResourceService';
import { SearchConfigResourceService } from './services/SearchConfigResourceService';
import { SuggestionCategoryResourceService } from './services/SuggestionCategoryResourceService';
import { TemplateResourceService } from './services/TemplateResourceService';
import { TriggerResourceService } from './services/TriggerResourceService';
import { TriggerWithDateResourceService } from './services/TriggerWithDateResourceService';

type HttpRequestConstructor = new (config: OpenAPIConfig) => BaseHttpRequest;

export class OpenApiRestClient {

    public readonly bucketResource: BucketResourceService;
    public readonly dataIndexResource: DataIndexResourceService;
    public readonly datasourceEventResource: DatasourceEventResourceService;
    public readonly datasourceResource: DatasourceResourceService;
    public readonly dateFilterResource: DateFilterResourceService;
    public readonly docTypeResource: DocTypeResourceService;
    public readonly enrichItemResource: EnrichItemResourceService;
    public readonly enrichPipelineResource: EnrichPipelineResourceService;
    public readonly keycloakSettingsResource: KeycloakSettingsResourceService;
    public readonly pipelineResource: PipelineResourceService;
    public readonly pluginDriverFieldResource: PluginDriverFieldResourceService;
    public readonly pluginDriverResource: PluginDriverResourceService;
    public readonly reindexResource: ReindexResourceService;
    public readonly schedulerResource: SchedulerResourceService;
    public readonly searchConfigResource: SearchConfigResourceService;
    public readonly suggestionCategoryResource: SuggestionCategoryResourceService;
    public readonly templateResource: TemplateResourceService;
    public readonly triggerResource: TriggerResourceService;
    public readonly triggerWithDateResource: TriggerWithDateResourceService;

    public readonly request: BaseHttpRequest;

    constructor(config?: Partial<OpenAPIConfig>, HttpRequest: HttpRequestConstructor = FetchHttpRequest) {
        this.request = new HttpRequest({
            BASE: config?.BASE ?? '',
            VERSION: config?.VERSION ?? '3.0.0-SNAPSHOT',
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
        this.datasourceEventResource = new DatasourceEventResourceService(this.request);
        this.datasourceResource = new DatasourceResourceService(this.request);
        this.dateFilterResource = new DateFilterResourceService(this.request);
        this.docTypeResource = new DocTypeResourceService(this.request);
        this.enrichItemResource = new EnrichItemResourceService(this.request);
        this.enrichPipelineResource = new EnrichPipelineResourceService(this.request);
        this.keycloakSettingsResource = new KeycloakSettingsResourceService(this.request);
        this.pipelineResource = new PipelineResourceService(this.request);
        this.pluginDriverFieldResource = new PluginDriverFieldResourceService(this.request);
        this.pluginDriverResource = new PluginDriverResourceService(this.request);
        this.reindexResource = new ReindexResourceService(this.request);
        this.schedulerResource = new SchedulerResourceService(this.request);
        this.searchConfigResource = new SearchConfigResourceService(this.request);
        this.suggestionCategoryResource = new SuggestionCategoryResourceService(this.request);
        this.templateResource = new TemplateResourceService(this.request);
        this.triggerResource = new TriggerResourceService(this.request);
        this.triggerWithDateResource = new TriggerWithDateResourceService(this.request);
    }
}
