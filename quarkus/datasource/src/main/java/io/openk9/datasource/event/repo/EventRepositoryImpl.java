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

package io.openk9.datasource.event.repo;

import io.openk9.datasource.event.util.Constants;
import io.openk9.datasource.event.util.QueryParameters;
import io.openk9.datasource.event.util.SortType;
import io.openk9.datasource.event.util.Sortable;
import io.openk9.datasource.model.Event;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.PreparedQuery;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ApplicationScoped
public class EventRepositoryImpl implements EventRepository {

	@Override
	public Uni<List<Event>> getEvents(
		List<String> fields, int from, int size,
		LinkedHashMap<String, Object> projections, List<? extends Sortable> sortBy,
		SortType sortType, boolean distinct) {

		return getEvents(
			fields, from, size, projections, sortBy, sortType, distinct,
			Event::from);

	}

	@Override
	public <T> Uni<List<T>> getEvents(
		List<String> fields, int from, int size,
		LinkedHashMap<String, Object> projections, List<? extends Sortable> sortBy,
		SortType sortType, boolean distinct, Function<Row, T> mapper) {

		PreparedQuery<RowSet<Row>> preparedQuery =
			client.preparedQuery(
				_createQuery(
					from, size, fields, projections, sortBy, sortType,
					distinct));

		return preparedQuery
			.execute(Tuple.from(new ArrayList<>(projections.values())))
			.onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
			.onItem().transform(mapper).collect().asList();
	}

	private String _createQuery(
		int from, int size, List<String> fields,
		LinkedHashMap<String, Object> projections,
		List<? extends Sortable> sortBy, SortType sortType, boolean distinct) {

		List<String> whereFields = new ArrayList<>(projections.keySet());

		List<String> whereConditions =
			IntStream.range(0, whereFields.size())
				.mapToObj(i -> _createWhere(whereFields, i))
				.collect(Collectors.toList());

		QueryParameters parameters;

		if (distinct) {
			parameters = QueryParameters.of(
				Event.TABLE_NAME, fields, fields, whereConditions, sortBy,
				sortType, size, from, false);
		}
		else {
			parameters = QueryParameters.of(
				Event.TABLE_NAME, fields, List.of(), whereConditions, sortBy,
				sortType, size, from, false);
		}

		TemplateInstance templateInstance =
			eventQuery.data("parameters", parameters);

		return templateInstance.render();

	}

	private static String _createWhere(List<String> keys, int i) {

		String key = keys.get(i);

		if (key.equals(Constants.GTE)) {
			return CREATED_GTE + (i + 1);
		}

		if (key.equals(Constants.LTE)) {
			return CREATED_LTE + (i + 1);
		}

		return key + " = $" + (i + 1);

	}

	@Inject
	io.vertx.mutiny.pgclient.PgPool client;

	@Inject
	Template eventQuery;

	public static final String CREATED_GTE = "created >= $";
	public static final String CREATED_LTE = "created <= $";

}
