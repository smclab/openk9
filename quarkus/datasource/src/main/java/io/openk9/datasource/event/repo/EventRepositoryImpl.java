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
	public Uni<List<Event>> getEvents(List<String> fields) {
		return getEvents(fields, 10000);
	}

	@Override
	public Uni<List<Event>> getEvents(List<String> fields, int size) {
		return null;
	}

	@Override
	public Uni<List<Event>> getEvents(
		List<String> fields, int size, LinkedHashMap<String, Object> projections) {

		String select = String.join(",", fields);

		PreparedQuery<RowSet<Row>> preparedQuery =
			client.preparedQuery(_createQuery(size, select, projections));

		return preparedQuery
			.execute(Tuple.from(new ArrayList<>(projections.values())))
			.onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
			.onItem().transform(this::_toEvent).collect().asList();

	}

	private static String _createQuery(
		int size, String select, LinkedHashMap<String, Object> projections) {

		String query = "SELECT " + select + " FROM event ";

		if (!projections.isEmpty()) {

			List<String> keys = new ArrayList<>(projections.keySet());

			query += IntStream
				.range(0, keys.size())
				.mapToObj(o -> keys.get(o) + " = $" + (o + 1))
				.collect(Collectors.joining(" AND ", "WHERE ", " "));

		}

		query += " ORDER BY created";

		if (size != -1) {
			query += " LIMIT " + size;
		}

		return query;
	}

	private Event _toEvent(Row row) {
		return row.toJson().mapTo(Event.class);
	}

	@Inject
	io.vertx.mutiny.pgclient.PgPool client;

}
