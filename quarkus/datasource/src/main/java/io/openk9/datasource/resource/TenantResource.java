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

package io.openk9.datasource.resource;

import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.model.dto.TenantDTO;
import io.openk9.datasource.resource.util.BaseK9EntityResource;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.TenantService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;

import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/tenants")
public class TenantResource extends
	BaseK9EntityResource<TenantService, Tenant, TenantDTO> {

	protected TenantResource(TenantService service) {
		super(service);
	}

	@GET
	@Path("/{id}/datasources")
	public Uni<Page<Datasource>> getDatasources(@PathParam("id")long tenantId, @BeanParam Pageable pageable) {
		return service.getDatasources(tenantId, pageable);
	}

	@PUT
	@Path("/{id}/datasources/{datasourceId}")
	public Uni<Tuple2<Tenant, Datasource>> addDatasource(@PathParam("id")long tenantId, @PathParam("datasourceId")long datasourceId) {
		return service.addDatasource(tenantId, datasourceId);
	}

	@DELETE
	@Path("/{id}/datasources/{datasourceId}")
	public Uni<Tuple2<Tenant, Datasource>> removeDatasource(@PathParam("id")long tenantId, @PathParam("datasourceId")long datasourceId) {
		return service.removeDatasource(tenantId, datasourceId);
	}

	@GET
	@Path("/{id}/suggestion-categories")
	public Uni<Page<SuggestionCategory>> getSuggestionCategories(@PathParam("id") long tenantId, @BeanParam Pageable pageable) {
		return service.getSuggestionCategories(tenantId, pageable);
	}

	@PUT
	@Path("/{id}/suggestion-categories/{suggestionCategoryId}")
	public Uni<Tuple2<Tenant, SuggestionCategory>> addSuggestionCategory(long tenantId, long suggestionCategoryId) {
		return service.addSuggestionCategory(tenantId, suggestionCategoryId);
	}

	@DELETE
	@Path("/{id}/suggestion-categories/{suggestionCategoryId}")
	public Uni<Tuple2<Tenant, SuggestionCategory>> removeSuggestionCategory(long tenantId, long suggestionCategoryId) {
		return service.removeSuggestionCategory(tenantId, suggestionCategoryId);
	}

}
