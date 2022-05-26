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

package io.openk9.datasource.event.graphql;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import io.openk9.datasource.event.repo.EventRepository;
import io.openk9.datasource.event.util.Operator;
import io.openk9.datasource.event.util.SortType;
import io.openk9.datasource.model.Event;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@GraphQLApi
@RequestScoped
public class GraphqlResource {

	@Query("event")
	@Description("Get Events")
	public Uni<List<Event>> getEvents(
		@Name("id") String id,
		@Name("type") String type,
		@Name("className") String className,
		@Name("groupKey") String groupKey,
		@Name("sortBy") @DefaultValue("CREATED") Event.Sortable sortBy,
		@Name("sortType") @DefaultValue("ASC") SortType sortType,
		@Name("gte") LocalDateTime gte,
		@Name("lte") LocalDateTime lte,
		@Name("size") @DefaultValue("10000") int size,
		@Name("from") @DefaultValue("0") int from,
		@Name("operator") @DefaultValue("AND") Operator operator) {

		DataFetchingEnvironment dfe =
			context.unwrap(DataFetchingEnvironment.class);

		DataFetchingFieldSelectionSet selectionSet = dfe.getSelectionSet();

		List<String> fields =
			selectionSet
				.getFields()
				.stream()
				.map(SelectedField::getQualifiedName)
				.collect(Collectors.toList());

		if (fields.isEmpty()) {
			return Uni.createFrom().item(List.of());
		}

		LinkedHashMap<String, Object> projections = new LinkedHashMap<>(4);

		if (id != null) {
			projections.put("id", id);
		}

		if (type != null) {
			projections.put("type", type);
		}

		if (className != null) {
			projections.put("className", className);
		}

		if (groupKey != null) {
			projections.put("groupKey", groupKey);
		}

		if (gte != null) {
			projections.put("gte", gte);
		}

		if (lte != null) {
			projections.put("lte", lte);
		}

		return eventRepository.getEvents(
			fields, from, size, projections, sortBy, sortType, operator);

	}
	@Inject
	SmallRyeContext context;

	@Inject
	io.vertx.mutiny.pgclient.PgPool client;

	@Inject
	EventRepository eventRepository;

}
