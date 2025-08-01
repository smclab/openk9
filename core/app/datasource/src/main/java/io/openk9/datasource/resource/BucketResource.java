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

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.dto.base.BucketDTO;
import io.openk9.datasource.resource.util.BaseK9EntityResource;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.BucketService;
import io.openk9.datasource.service.util.Tuple2;

import io.smallrye.mutiny.Uni;

@Path("/buckets")
@RolesAllowed("k9-admin")
@Deprecated
public class BucketResource extends
	BaseK9EntityResource<BucketService, Bucket, BucketDTO> {

	protected BucketResource(BucketService service) {
		super(service);
	}

	@GET
	@Path("/{id}/datasources")
	public Uni<Page<Datasource>> getDatasources(
		@PathParam("id")long bucketId, @BeanParam Pageable pageable,
		@QueryParam("searchText") String searchText) {
		return service.getDatasources(bucketId, pageable, searchText);
	}

	@PUT
	@Path("/{id}/datasources/{datasourceId}")
	public Uni<Tuple2<Bucket, Datasource>> addDatasource(@PathParam("id")long bucketId, @PathParam("datasourceId")long datasourceId) {
		return service.addDatasource(bucketId, datasourceId);
	}

	@DELETE
	@Path("/{id}/datasources/{datasourceId}")
	public Uni<Tuple2<Bucket, Datasource>> removeDatasource(@PathParam("id")long bucketId, @PathParam("datasourceId")long datasourceId) {
		return service.removeDatasource(bucketId, datasourceId);
	}

	@GET
	@Path("/{id}/suggestion-categories")
	public Uni<Page<SuggestionCategory>> getSuggestionCategories(
		@PathParam("id") long bucketId, @BeanParam Pageable pageable,
		@QueryParam("searchText") String searchText) {
		return service.getSuggestionCategories(bucketId, pageable, searchText);
	}

	@PUT
	@Path("/{id}/suggestion-categories/{suggestionCategoryId}")
	public Uni<Tuple2<Bucket, SuggestionCategory>> addSuggestionCategory(
		@PathParam("id") long bucketId,
		@PathParam("suggestionCategoryId") long suggestionCategoryId) {

		return service.addSuggestionCategory(bucketId, suggestionCategoryId);
	}

	@DELETE
	@Path("/{id}/suggestion-categories/{suggestionCategoryId}")
	public Uni<Tuple2<Bucket, SuggestionCategory>> removeSuggestionCategory(
		@PathParam("id") long bucketId,
		@PathParam("suggestionCategoryId") long suggestionCategoryId) {

		return service.removeSuggestionCategory(bucketId, suggestionCategoryId);
	}

}
