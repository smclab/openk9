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
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import io.openk9.datasource.event.dto.EventOption;
import io.openk9.datasource.event.storage.Event;
import io.openk9.datasource.event.storage.EventStorageRepository;
import io.openk9.datasource.event.util.Constants;
import io.openk9.datasource.event.util.SortType;
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
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

			Optional<Event> eventById =
				eventStorageRepository.getEventById(UUID.fromString(id));

			if (eventById.isPresent()) {
				Event event = eventById.get();
				String dataPath = event.getDataPath();
				int dataSize = event.getDataSize();
				byte[] bytes;
				try {
					bytes = Files.readAllBytes(Paths.get(dataPath));
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
				return new String(Zstd.decompress(bytes, dataSize));
			}

			return "";

		});

	}

	@Query("eventOptions")
	@Description("Returns the list of available options for the event")
	public Uni<List<EventOption>> eventOptions(
		@Name("sortable") @DefaultValue("true") boolean sortable,
		@Name("sortType") @DefaultValue("ASC") SortType sortType,
		@Name("size") @DefaultValue("20") int size,
		@Name("from") @DefaultValue("0") int from
	) {

		return Uni.createFrom().item(List.of());

	}

	@Query("event")
	@Description("Returns the list of events")
	public Uni<List<Event>> getEvents(
		@Description("Primary key of event") @Name("id") String id,
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
			.item(() -> eventStorageRepository.getEvents(from, size));

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

	@Inject
	EventStorageRepository eventStorageRepository;

}
