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

package io.openk9.datasource.cache;

import com.hazelcast.map.MapStoreAdapter;
import io.openk9.datasource.cache.model.Event;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowIterator;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class EventMapStore extends MapStoreAdapter<UUID, Event> {

	@Override
	public void delete(UUID key) {
		client
			.preparedQuery(Event.DELETE_BY_ID)
			.executeAndAwait(Tuple.of(key));
	}

	@Override
	public Event load(UUID key) {

		RowSet<Row> rows = client
			.preparedQuery(Event.SELECT_BY_ID)
			.executeAndAwait(Tuple.of(key));

		RowIterator<Row> iterator = rows.iterator();

		if (iterator.hasNext()) {
			return Event.from(iterator.next());
		}

		return null;
	}

	@Override
	public void store(UUID key, Event event) {

		client
			.preparedQuery(Event.INSERT_QUERY)
			.executeAndAwait(_toTuple(event));
	}

	@Override
	public void storeAll(Map<UUID, Event> map) {

		Collection<Event> values = map.values();

		client
			.preparedQuery(Event.INSERT_QUERY)
			.executeBatchAndAwait(
				values
					.stream()
					.map(this::_toTuple)
					.collect(Collectors.toList())
			);

	}

	@Override
	public Iterable<UUID> loadAllKeys() {

		return new Iterable<>() {
			private final Iterator<Row> iterable =
				client
					.preparedQuery(Event.SELECT_ID)
					.executeAndAwait()
					.iterator();

			@Override
			public Iterator<UUID> iterator() {
				return new Iterator<>() {
					@Override
					public boolean hasNext() {
						return iterable.hasNext();
					}

					@Override
					public UUID next() {
						return iterable.next().getUUID(0);
					}
				};
			}
		};
	}

	private Tuple _toTuple(Event event) {
		return Tuple.from(
			Arrays.asList(
				event.getId(),
				event.getType(),
				event.getGroupKey(),
				event.getClassName(),
				event.getClassPK(),
				event.getParsingDate(),
				event.getCreated(),
				event.getSize()
			)
		);
	}

	@Inject
	PgPool client;

}
