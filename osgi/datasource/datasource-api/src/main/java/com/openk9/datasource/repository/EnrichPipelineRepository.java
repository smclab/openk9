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

package com.openk9.datasource.repository;

import com.openk9.datasource.model.EnrichPipeline;
import com.openk9.sql.api.entity.ReactiveRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EnrichPipelineRepository extends
	ReactiveRepository<EnrichPipeline, Long> {

	public Mono<EnrichPipeline> removeEnrichPipeline(Long enrichPipelineId);
	public Mono<EnrichPipeline> findByDatasourceId(Long datasourceId);
	public Mono<EnrichPipeline> findByEnrichPipelineIdAndDatasourceId(Long datasourceId, Long enrichPipelineId);
	public Mono<EnrichPipeline> addEnrichPipeline(EnrichPipeline enrichpipeline);
	public Mono<EnrichPipeline> addEnrichPipeline(Boolean active, Long datasourceId, String name);
	public Mono<EnrichPipeline> updateEnrichPipeline(EnrichPipeline enrichpipeline);
	public Mono<EnrichPipeline> updateEnrichPipeline(Boolean active, Long datasourceId, Long enrichPipelineId, String name);
	public Mono<EnrichPipeline> removeEnrichPipeline(EnrichPipeline enrichpipeline);
	public Mono<EnrichPipeline> findByPrimaryKey(Long enrichPipelineId);
	public Flux<EnrichPipeline> findAll();

}