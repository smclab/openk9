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

package io.openk9.datasource.event.model;

import io.openk9.datasource.event.util.Constants;
import io.openk9.datasource.event.util.Sortable;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "event", indexes = {
	@Index(name = "idx_event_type", columnList = "type"),
	@Index(name = "idx_event_groupKey", columnList = "groupKey"),
	@Index(name = "idx_event_className", columnList = "className"),
	@Index(name = "idx_event_classPK", columnList = "classPK"),
	@Index(name = "idx_event_classPK_groupKey", columnList = "classPK, groupKey"),
	@Index(name = "idx_event_groupkey_classname", columnList = "groupKey, className"),
	@Index(name = "idx_event_type_classname", columnList = "type, className"),
	@Index(name = "idx_event_type_groupkey", columnList = "type, groupKey"),
	@Index(name = "idx_event_type_groupKey_className", columnList = "type, groupKey, className")
})
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Cacheable
@Builder
public class Event extends PanacheEntityBase {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = ID, nullable = false)
	private UUID id;

	@Column(name = TYPE)
	private String type;

	@Lob
	@Column(name = DATA)
	private String data;

	@Column(name = SIZE)
	private Integer size;

	@Setter(AccessLevel.NONE)
	@Version
	@Column(name = VERSION)
	private Integer version;

	@Column(name = CREATED, nullable = false)
	private LocalDateTime created = LocalDateTime.now();

	@Column(name = PARSING_DATE)
	private LocalDateTime parsingDate;

	@Column(name = GROUP_KEY)
	private String groupKey;

	@Column(name = CLASS_PK)
	private String classPK;

	@Column(name = CLASS_NAME)
	private String className;

	public static Uni<List<Event>> getEventsBetween(
		Temporal gte, Temporal lte, int maxResult) {

		PanacheQuery<PanacheEntityBase> query =
			find(
				"created >= :gte AND created < :lte",
				Map.of(
					Constants.GTE, LocalDateTime.from(gte),
					Constants.LTE, LocalDateTime.from(lte)
				),
				Sort.by(CREATED));

		return _getEvents(maxResult, query);

	}

	public static Uni<List<Event>> getEventsLte(Temporal lte) {
		return getEventsLte(lte, MAX_RESULT);
	}

	public static Uni<List<Event>> getEventsLte(Temporal lte, int maxResult) {

		PanacheQuery<PanacheEntityBase> query =
			find(
				"created <= :lte",
				Parameters.with(Constants.LTE, LocalDateTime.from(lte)),
				Sort.by(CREATED));

		return _getEvents(maxResult, query);

	}

	public static Uni<List<Event>> getEventsGte(Temporal gte) {
		return getEventsGte(gte, MAX_RESULT);
	}

	public static Uni<List<Event>> getEventsGte(Temporal gte, int maxResult) {

		PanacheQuery<PanacheEntityBase> query =
			find(
				"created >= :gte",
				Parameters.with(Constants.GTE, LocalDateTime.from(gte)),
				Sort.by(CREATED));

		return _getEvents(maxResult, query);

	}

	public static Uni<List<Event>> getEvents(String type) {
		return getEvents(type, MAX_RESULT);
	}

	public static Uni<List<Event>> getEvents(String type, int maxResult) {

		PanacheQuery<PanacheEntityBase> query =
			find(TYPE, type, Sort.by(CREATED));

		return _getEvents(maxResult, query);

	}

	public static Uni<List<Event>> getEvents(
		String type, String groupKey, LocalDateTime lte) {

		PanacheQuery<PanacheEntityBase> query =
			find(
				"type = :type AND groupKey = :groupKey AND created <= :lte",
				Map.of(Constants.LTE, lte, TYPE, type, GROUP_KEY, groupKey),
				Sort.by(CREATED));

		return query.list();

	}

	public static Uni<List<Event>> getEvents() {
		return getEvents(MAX_RESULT);
	}

	public static Uni<List<Event>> getEvents(int maxResult) {

		PanacheQuery<PanacheEntityBase> query =
			findAll(Sort.by(CREATED));

		return _getEvents(maxResult, query);
	}

	public static Event from(Row row) {

		EventBuilder builder = Event.builder();

		for (int i = 0; i < row.size(); i++) {
			String columnName = row.getColumnName(i);
			if (columnName.equalsIgnoreCase(ID)) {
				builder.id(row.getUUID(i));
			}
			else if (columnName.equalsIgnoreCase(TYPE)) {
				builder.type(row.getString(i));
			}
			else if (columnName.equalsIgnoreCase(DATA)) {
				builder.data(row.getString(i));
			}
			else if (columnName.equalsIgnoreCase(SIZE)) {
				builder.size(row.getInteger(i));
			}
			else if (columnName.equalsIgnoreCase(VERSION)) {
				builder.version(row.getInteger(i));
			}
			else if (columnName.equalsIgnoreCase(CREATED)) {
				builder.created(row.getLocalDateTime(i));
			}
			else if (columnName.equalsIgnoreCase(GROUP_KEY)) {
				builder.groupKey(row.getString(i));
			}
			else if (columnName.equalsIgnoreCase(CLASS_NAME)) {
				builder.className(row.getString(i));
			}
		}

		return builder.build();

	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || Hibernate.getClass(this) !=
						 Hibernate.getClass(o)) {
			return false;
		}
		Event event = (Event) o;
		return id != null && Objects.equals(id, event.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	private static Uni<List<Event>> _getEvents(
		int maxResult, PanacheQuery<PanacheEntityBase> query) {

		if (maxResult == -1) {
			return query.list();
		}

		return query
			.page(Page.ofSize(maxResult))
			.list();

	}

	public static final LocalDateTime NULL_DATE = LocalDateTime.of(
		1970, 1, 1, 0, 0, 0);

	public static final int MAX_RESULT = 10_000;
	public static final String TABLE_NAME = "event";
	public static final String ID = "id";
	public static final String TYPE = "type";
	public static final String DATA = "data";
	public static final String SIZE = "size";
	public static final String VERSION = "version";
	public static final String CREATED = "created";
	public static final String GROUP_KEY = "groupKey";
	public static final String CLASS_NAME = "className";
	public static final String PARSING_DATE = "parsingDate";
	public static final String CLASS_PK = "classPK";

	public enum EventSortable implements Sortable {
		TYPE(Event.TYPE),
		SIZE(Event.SIZE),
		CREATED(Event.CREATED),
		PARSING_DATE(Event.PARSING_DATE),
		GROUP_KEY(Event.GROUP_KEY),
		CLASS_NAME(Event.CLASS_NAME);

		EventSortable(String column) {
			this.column = column;
		}

		@Override
		public String getColumn() {
			return column;
		}

		private final String column;
	}

}