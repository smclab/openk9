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

package io.openk9.datasource.service;

import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.processor.payload.DatasourceContext;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class DatasourceService {

	@CacheResult(cacheName = "datasource-context-cache")
	public Uni<DatasourceContext> getDatasourceContext(long datasourceId) {

		Uni<Datasource> datasourceUni = getDatasource(datasourceId);

		return datasourceUni.flatMap(datasource ->
			getEnrichPipelineByDatasourceId(datasource.getDatasourceId())
				.flatMap(enrichPipeline -> {
					Uni<List<EnrichItem>> enrichItemUni;

					if (enrichPipeline.getEnrichPipelineId() != null) {

						enrichItemUni =
							getEnrichItemByEnrichPipelineId(
								enrichPipeline.getEnrichPipelineId());

					}
					else {
						enrichItemUni = Uni.createFrom().item(List.of());
					}

					return Uni
						.combine()
						.all()
						.unis(
							getTenant(datasource.getTenantId()),
							enrichItemUni)
						.combinedWith((tenant, enrichItemList) ->
							DatasourceContext.of(
								datasource, tenant,
								enrichPipeline, enrichItemList
							));

				})
		);

	}
	@CacheResult(cacheName = "datasource-cache")
	public Uni<Datasource> getDatasource(Long id) {
		return Datasource.findById(id);
	}

	@CacheResult(cacheName = "enrich-pipeline-cache")
	public Uni<EnrichPipeline> getEnrichPipelineByDatasourceId(
		Long datasourceId) {
		return EnrichPipeline
			.findByDatasourceId(datasourceId)
			.onItem()
			.ifNull()
			.continueWith(EnrichPipeline::new);
	}

	@CacheResult(cacheName = "enrich-item-cache")
	public Uni<List<EnrichItem>> getEnrichItemByEnrichPipelineId(
		Long enrichPipelineId) {
		return EnrichItem
			.findByEnrichPipelineId(enrichPipelineId)
			.onItem()
			.ifNull()
			.continueWith(List::of);
	}

	@CacheResult(cacheName = "tenant-cache")
	public Uni<Tenant> getTenant(Long tenantId) {
		return Tenant.findById(tenantId);
	}

	@CacheInvalidateAll.List(
		value = {
			@CacheInvalidateAll(cacheName = "datasource-context-cache"),
			@CacheInvalidateAll(cacheName = "datasource-cache"),
			@CacheInvalidateAll(cacheName = "enrich-pipeline-cache"),
			@CacheInvalidateAll(cacheName = "enrich-item-cache"),
			@CacheInvalidateAll(cacheName = "tenant-cache")
		}
	)
	@Scheduled(every="120s")
	public void invalidateCache() {
	}

}
