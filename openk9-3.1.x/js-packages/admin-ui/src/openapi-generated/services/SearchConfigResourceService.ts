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

