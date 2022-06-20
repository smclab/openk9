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

import com.github.luben.zstd.Zstd;
import com.hazelcast.aggregation.Aggregators;
import com.hazelcast.map.IMap;
import com.hazelcast.projection.Projection;
import com.hazelcast.projection.Projections;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;
import com.hazelcast.query.QueryConstants;
import com.hazelcast.query.impl.predicates.TruePredicate;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import io.openk9.datasource.cache.annotation.MapName;
import io.openk9.datasource.cache.dto.EventDTO;
import io.openk9.datasource.cache.model.Event;
import io.openk9.datasource.event.dto.EventOption;
import io.openk9.datasource.event.sender.StorageConfig;
import io.openk9.datasource.event.util.Constants;
import io.openk9.datasource.event.util.SortType;
import io.openk9.datasource.mapper.EventMapper;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@GraphQLApi
@RequestScoped
public class GraphqlResource {

	@Query("eventData")
	@Description("Get event data")
	public Uni<String> getEventData(
			@Description("Primary key of event") @Name("id") String id)
		throws Exception {

		if (id == null || id.isBlank()) {
			return Uni.createFrom().nothing();
		}

		return Uni.createFrom().item(() -> {

			Event eventById =
				eventMap.get(UUID.fromString(id));

			if (eventById == null) {
				return "";
			}

			Path path = Paths.get(
				storageConfig.getStorageDir(), eventById.getId().toString());

			int dataSize = eventById.getSize();

			try {
				return new String(Zstd.decompress(Files.readAllBytes(path), dataSize));
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}

		});

	}

	@Query("eventOptions")
	@Description("Returns the list of available options for the event")
	public Uni<Collection<EventOption>> eventOptions(
		@Name("sortable") @DefaultValue("true") boolean sortable,
		@Name("sortType") @DefaultValue("ASC") SortType sortType,
		@Name("size") @DefaultValue("20") int size,
		@Name("from") @DefaultValue("0") int from
	) {

		List<String> fields = _getFieldsFromContext();

		if (fields.isEmpty()) {
			return Uni.createFrom().item(List.of());
		}

		return Uni.createFrom().item(() -> {

			Set<EventOption> eventOptions = new HashSet<>();

			for (String field : fields) {
				Set<String> aggregate =
					eventMap.aggregate(Aggregators.distinct(field));
				eventOptions.addAll(_toEventOption(field, aggregate));
			}

			return eventOptions;

		});

	}

	private Collection<? extends EventOption> _toEventOption(
		String field, Set<String> aggregate) {

		List<EventOption> eventOptions = new ArrayList<>(aggregate.size());

		for (String value : aggregate) {
			EventOption.EventOptionBuilder builder = EventOption.builder();
			switch (field) {
				case Event.TYPE:
					builder.type(value);
					break;
				case Event.CLASS_NAME:
					builder.className(value);
					break;
				case Event.GROUP_KEY:
					builder.groupKey(value);
					break;
				case Event.CLASS_PK:
					builder.classPK(value);
					break;
			}
			eventOptions.add(builder.build());
		}

		return eventOptions;

	}

	@Query("event")
	@Description("Returns the list of events")
	public Uni<List<EventDTO>> getEvents(
		@Description("Primary key of event") @Name(Event.ID) String id,
		@Description("Type of event (INGESTION, INGESTION_DATASOURCE)") @Name(Event.TYPE) String type,
		@Description("class name of event") @Name(Event.CLASS_NAME) String className,
		@Description("event group key set") @Name(Event.GROUP_KEY) String groupKey,
		@Description("primary key of the event") @Name(Event.CLASS_PK) String classPK,
		@Name("sortBy") @DefaultValue("CREATED") Event.EventSortable sortBy,
		@Name("sortType") @DefaultValue("ASC") SortType sortType,
		@Name(Constants.GTE) LocalDateTime gte,
		@Name(Constants.LTE) LocalDateTime lte,
		@Name("size") @DefaultValue("10000") int size,
		@Name("from") @DefaultValue("0") int from) {

		List<String> fields = _getFieldsFromContext();

		if (fields.isEmpty()) {
			return Uni.createFrom().item(List.of());
		}
		return Uni
			.createFrom()
			.item(() -> {

				Predicate predicate = _createPredicates(
					id, type, className, groupKey, classPK, gte, lte);

				Set<UUID> keys;

				if (predicate == TruePredicate.INSTANCE) {
					keys =  eventMap.localKeySet();
				}
				else {
					keys = eventMap.localKeySet(predicate);
				}

				UUID[] uuids = _subList(keys, size, from);

				Projection<Object, Object[]> projection = Projections
					.multiAttribute(fields.toArray(String[]::new));

				eventMap.project(
					projection, Predicates.in(
						QueryConstants.KEY_ATTRIBUTE_NAME.value(), uuids));

				List<EventDTO> eventDTOS = _objectsToEvents(
					fields, eventMap.project(
						projection, Predicates.in(
							QueryConstants.KEY_ATTRIBUTE_NAME.value(), uuids)));

				// eventDTOS.sort(sortBy.getComparator(sortType));

				return eventDTOS;

			});

	}

	private UUID[] _subList(Set<UUID> keys, int size, int from) {
		return keys.stream()
			.skip(from)
			.limit(size)
			.toArray(UUID[]::new);
	}

	private List<EventDTO> _objectsToEvents(
		List<String> fields, Collection<Object[]> response) {

		List<EventDTO> events = new ArrayList<>(response.size());

		for (Object[] objects : response) {
			EventDTO.EventDTOBuilder builder = EventDTO.builder();
			for (int i = 0; i < fields.size(); i++) {
				String field = fields.get(i);
				switch (field) {
					case Event.ID:
						builder.id((UUID) objects[i]);
						break;
					case Event.CLASS_NAME:
						builder.className((String) objects[i]);
						break;
					case Event.TYPE:
						builder.type((String) objects[i]);
						break;
					case Event.GROUP_KEY:
						builder.groupKey((String) objects[i]);
						break;
					case Event.CLASS_PK:
						builder.classPK((String) objects[i]);
						break;
					case Event.SIZE:
						builder.size((Integer) objects[i]);
						break;
					case Event.CREATED:
						builder.created((LocalDateTime) objects[i]);
						break;
					case Event.PARSING_DATE:
						builder.parsingDate((LocalDateTime) objects[i]);
						break;
					case Event.VERSION:
						builder.version((Integer) objects[i]);
						break;
				}
			}
			events.add(builder.build());
		}

		return events;
	}

	private Predicate _createPredicates(
		String id, String type, String className, String groupKey,
		String classPK, LocalDateTime gte, LocalDateTime lte) {

		List<Predicate> predicates = new ArrayList<>();

		if (id != null && !id.isBlank()) {
			predicates.add(Predicates.equal(Event.ID, UUID.fromString(id)));
		}

		if (type != null && !type.isBlank()) {
			predicates.add(Predicates.equal(Event.TYPE, type));
		}

		if (className != null && !className.isBlank()) {
			predicates.add(Predicates.equal(Event.CLASS_NAME, className));
		}

		if (groupKey != null && !groupKey.isBlank()) {
			predicates.add(Predicates.equal(Event.GROUP_KEY, groupKey));
		}

		if (classPK != null && !classPK.isBlank()) {
			predicates.add(Predicates.equal(Event.CLASS_PK, classPK));
		}

		if (gte != null && lte != null) {
			predicates.add(Predicates.between(Event.CREATED, gte, lte));
		}
		else {
			if (gte != null) {
				predicates.add(Predicates.greaterEqual(Event.CREATED, gte));
			}
			if (lte != null) {
				predicates.add(Predicates.lessEqual(Event.CREATED, lte));
			}
		}

		if (predicates.isEmpty()) {
			return TruePredicate.INSTANCE;
		}

		return Predicates.and(predicates.toArray(Predicate[]::new));

	}

	private List<String> _getFieldsFromContext() {

		DataFetchingEnvironment dfe =
			context.unwrap(DataFetchingEnvironment.class);

		DataFetchingFieldSelectionSet selectionSet = dfe.getSelectionSet();

		return selectionSet
			.getFields()
			.stream()
			.map(SelectedField::getQualifiedName)
			.collect(Collectors.toList());

	}

	@Inject
	SmallRyeContext context;

	@MapName("eventMap")
	@Inject
	IMap<UUID, Event> eventMap;

	@Inject
	StorageConfig storageConfig;

	@Inject
	EventMapper mapper;

}
