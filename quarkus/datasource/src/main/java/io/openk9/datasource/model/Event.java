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

package io.openk9.datasource.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "event", indexes = {
	@Index(name = "idx_event_type", columnList = "type"),
	@Index(name = "idx_event_groupKey", columnList = "groupKey"),
	@Index(name = "idx_event_className", columnList = "className"),
	@Index(name = "idx_event_groupkey_classname", columnList = "groupKey, className"),
	@Index(name = "idx_event_type_classname", columnList = "type, className"),
	@Index(name = "idx_event_type_groupkey", columnList = "type, groupKey"),
	@Index(name = "idx_event_type_groupKey_className", columnList = "type, groupKey, className")
})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Cacheable
@Builder
public class Event extends PanacheEntityBase {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "type")
	private String type;

	@Lob
	@Column(name = "data")
	private String data;

	@Setter(AccessLevel.NONE)
	@Version
	@Column(name = "version")
	private Integer version;

	@Column(name = "created", nullable = false)
	private final LocalDateTime created = LocalDateTime.now();

	@Column(name = "groupKey")
	private String groupKey;

	@Column(name = "className")
	private String className;

	public static Uni<List<Event>> getEvents(
		String type, String groupKey, String className, int size) {

		Map<String, Object> params = new HashMap<>();

		List<String> strings = new ArrayList<>();

		if (type != null) {
			params.put("type", type);
		}

		if (groupKey != null) {
			params.put("groupKey", groupKey);
		}

		if (className != null) {
			params.put("className", className);
		}

		String query = params.keySet().stream().map(e -> e + " = :" + e).collect(
			Collectors.joining(" AND "));

		if (query.isBlank()) {
			return getEvents();
		}

		return find(query, Sort.by("created"), params)
			.page(0, size).list();
	}

	public static Uni<List<Event>> getEventsBetween(
		Temporal gte, Temporal lte) {

		return getEventsBetween(gte, lte, MAX_RESULT);
	}

	public static Uni<List<Event>> getEventsBetween(
		Temporal gte, Temporal lte, int maxResult) {

		PanacheQuery<PanacheEntityBase> query =
			find(
				"created >= :gte AND created < :lte",
				Map.of(
					"gte", LocalDateTime.from(gte),
					"lte", LocalDateTime.from(lte)
				),
				Sort.by("created"));

		return _getEvents(maxResult, query);

	}

	public static Uni<List<Event>> getEventsLte(Temporal lte) {
		return getEventsLte(lte, MAX_RESULT);
	}

	public static Uni<List<Event>> getEventsLte(Temporal lte, int maxResult) {

		PanacheQuery<PanacheEntityBase> query =
			find(
				"created <= :lte",
				Parameters.with("lte", LocalDateTime.from(lte)),
				Sort.by("created"));

		return _getEvents(maxResult, query);

	}

	public static Uni<List<Event>> getEventsGte(Temporal gte) {
		return getEventsGte(gte, MAX_RESULT);
	}

	public static Uni<List<Event>> getEventsGte(Temporal gte, int maxResult) {

		PanacheQuery<PanacheEntityBase> query =
			find(
				"created >= :gte",
				Parameters.with("gte", LocalDateTime.from(gte)),
				Sort.by("created"));

		return _getEvents(maxResult, query);

	}

	public static Uni<List<Event>> getEvents(String type) {
		return getEvents(type, MAX_RESULT);
	}

	public static Uni<List<Event>> getEvents(String type, int maxResult) {

		PanacheQuery<PanacheEntityBase> query =
			find("type", type, Sort.by("created"));

		return _getEvents(maxResult, query);

	}

	public static Uni<List<Event>> getEvents() {
		return getEvents(MAX_RESULT);
	}

	public static Uni<List<Event>> getEvents(int maxResult) {

		PanacheQuery<PanacheEntityBase> query =
			findAll(Sort.by("created"));

		return _getEvents(maxResult, query);
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

	public static final int MAX_RESULT = 10_000;

}