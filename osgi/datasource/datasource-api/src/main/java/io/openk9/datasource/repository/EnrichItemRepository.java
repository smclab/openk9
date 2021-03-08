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

package io.openk9.datasource.repository;

import io.openk9.datasource.model.EnrichItem;
import io.openk9.sql.api.client.Page;
import io.openk9.sql.api.client.Sort;
import io.openk9.sql.api.entity.ReactiveRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EnrichItemRepository extends
	ReactiveRepository<EnrichItem, Long> {

	public Mono<EnrichItem> removeEnrichItem(Long enrichItemId);
	public Flux<EnrichItem> findByEnrichPipelineId(Long enrichPipelineId);
	public Flux<EnrichItem> findByEnrichPipelineId(Long enrichPipelineId, Page page);
	public Flux<EnrichItem> findByActiveAndEnrichPipelineId(Boolean active, Long enrichPipelineId);
	public Flux<EnrichItem> findByActiveAndEnrichPipelineId(Boolean active, Long enrichPipelineId, Page page);
	public Flux<EnrichItem> findByActiveAndEnrichPipelineId(Boolean active, Long enrichPipelineId, Sort...sorts);
	public Mono<EnrichItem> addEnrichItem(EnrichItem enrichitem);
	public Mono<EnrichItem> addEnrichItem(Integer _position, Boolean active, String jsonConfig, Long enrichPipelineId, String name, String serviceName);
	public Mono<EnrichItem> updateEnrichItem(EnrichItem enrichitem);
	public Mono<EnrichItem> updateEnrichItem(Integer _position, Boolean active, String jsonConfig, Long enrichItemId, Long enrichPipelineId, String name, String serviceName);
	public Mono<EnrichItem> removeEnrichItem(EnrichItem enrichitem);
	public Mono<EnrichItem> findByPrimaryKey(Long enrichItemId);
	public Flux<EnrichItem> findAll();

}