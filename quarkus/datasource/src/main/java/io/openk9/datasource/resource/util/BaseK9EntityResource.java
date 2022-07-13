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

package io.openk9.datasource.resource.util;

import io.openk9.datasource.graphql.util.SortType;
import io.openk9.datasource.model.dto.util.K9EntityDTO;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.jboss.resteasy.reactive.RestStreamElementType;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@CircuitBreaker
@ReactiveTransactional
public abstract class BaseK9EntityResource<
	SERVICE extends BaseK9EntityService<ENTITY, DTO>,
	ENTITY extends K9Entity,
	DTO extends K9EntityDTO> {

	protected BaseK9EntityResource(SERVICE service) {
		this.service = service;
	}

	@GET
	public Uni<Page<ENTITY>> findAll(
		@QueryParam("limit") @DefaultValue("20") int limit,
		@QueryParam("offset") @DefaultValue("0") int offset,
		@QueryParam("sortBy") @DefaultValue("createDate") String sortBy,
		@QueryParam("sortType") @DefaultValue("ASC") SortType sortType) {
		return this.service.findAllPaginated(limit, offset, sortBy, sortType);
	}

	@GET
	@Path("/count")
	public Uni<Long> count() {
		return this.service.count();
	}

	@GET
	@Path("/{id}")
	public Uni<ENTITY> findById(@PathParam("id") long id) {
		return this.service.findById(id);
	}

	@PATCH
	@Path("/{id}")
	public Uni<ENTITY> patch(@PathParam("id") long id, DTO dto) {
		return this.service.patch(id, dto);
	}

	@PUT
	@Path("/{id}")
	public Uni<ENTITY> update(@PathParam("id") long id, DTO dto) {
		return this.service.update(id, dto);
	}

	@POST
	public Uni<ENTITY> persist(DTO entity) {
		return this.service.persist(entity);
	}

	@DELETE
	@Path("/{id}")
	public Uni<ENTITY> deleteById(@PathParam("id") long entityId) {
		return this.service.deleteById(entityId);
	}

	@GET
	@Path("/stream")
	@Produces(MediaType.SERVER_SENT_EVENTS)
	@RestStreamElementType(MediaType.APPLICATION_JSON)
	public Multi<K9EntityEvent<ENTITY>> getProcessor() {
		return this.service.getProcessor();
	}

	protected final SERVICE service;

}
