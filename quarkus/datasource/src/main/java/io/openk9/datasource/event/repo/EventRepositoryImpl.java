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

import io.openk9.datasource.event.util.Operator;
import io.openk9.datasource.event.util.SortType;
import io.openk9.datasource.model.Event;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ApplicationScoped
public class EventRepositoryImpl implements EventRepository {

	@Override
	public Uni<List<Event>> getEvents(
		List<String> fields, int from, int size,
		LinkedHashMap<String, Object> projections, Event.Sortable sortBy,
		SortType sortType, Operator operator) {

		String select = String.join(",", fields);

		PreparedQuery<RowSet<Row>> preparedQuery =
			client.preparedQuery(
				_createQuery(
					from, size, select, projections, sortBy, sortType, operator));

		return preparedQuery
			.execute(Tuple.from(new ArrayList<>(projections.values())))
			.onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
			.onItem().transform(this::_toEvent).collect().asList();

	}

	private static String _createQuery(
		int from, int size, String select, LinkedHashMap<String, Object> projections,
		Event.Sortable sortBy, SortType sortType, Operator operator) {

		String query = "SELECT " + select + " FROM event ";

		if (!projections.isEmpty()) {

			List<String> keys = new ArrayList<>(projections.keySet());

			String operatorS = " " + operator.name() + " ";

			query += IntStream
				.range(0, keys.size())
				.mapToObj(i -> _createWhere(keys, i))
				.collect(Collectors.joining(operatorS, "WHERE ", " "));

		}

		query += " ORDER BY " + sortBy.getColumn() + " " + sortType.name();

		if (from > 0) {
			query += " OFFSET " + from;
		}

		if (size > 0) {
			query += " LIMIT " + size;
		}

		return query;
	}

	private static String _createWhere(List<String> keys, int i) {

		String key = keys.get(i);

		if (key.equals("gte")) {
			return "created >= $" + (i + 1);
		}

		if (key.equals("lte")) {
			return "created <= $" + (i + 1);
		}

		return key + " = $" + (i + 1);

	}

	private Event _toEvent(Row row) {
		return row.toJson().mapTo(Event.class);
	}

	@Inject
	io.vertx.mutiny.pgclient.PgPool client;

}
