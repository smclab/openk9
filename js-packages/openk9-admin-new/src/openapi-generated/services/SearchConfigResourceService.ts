/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { HybridSearchPipelineDTO } from '../models/HybridSearchPipelineDTO';
import type { SearchPipelineResponseDTO } from '../models/SearchPipelineResponseDTO';

import type { CancelablePromise } from '../core/CancelablePromise';
import type { BaseHttpRequest } from '../core/BaseHttpRequest';

export class SearchConfigResourceService {

    constructor(public readonly httpRequest: BaseHttpRequest) {}

    /**
     * Configure Hybrid Search
     * @param id 
     * @param requestBody 
     * @returns SearchPipelineResponseDTO OK
     * @throws ApiError
     */
    public postApiDatasourceV1SearchConfigConfigureHybridSearch(
id: number,
requestBody: HybridSearchPipelineDTO,
): CancelablePromise<SearchPipelineResponseDTO> {
        return this.httpRequest.request({
            method: 'POST',
            url: '/api/datasource/v1/search-config/{id}/configure-hybrid-search',
            path: {
                'id': id,
            },
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Bad Request`,
            },
        });
    }

}
