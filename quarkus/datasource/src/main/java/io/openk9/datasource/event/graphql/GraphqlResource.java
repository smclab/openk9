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
import io.openk9.datasource.event.model.Event;
import io.openk9.datasource.event.repo.EventRepository;
import io.openk9.datasource.event.util.Constants;
import io.openk9.datasource.event.util.SortType;
import io.openk9.datasource.mapper.EventMapper;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@GraphQLApi
@RequestScoped
@CircuitBreaker
public class GraphqlResource {

	@Query("eventData")
	@Description("Get event data")
	public Uni<String> getEventData(
			@Description("Primary key of event") @Name("id") String id) {

		if (id == null || id.isBlank()) {
			return Uni.createFrom().nothing();
		}

		return eventRepository
			.findById(id, new String[]{"data", "size"})
			.map(event -> new String(Zstd.decompress(event.getData(), event.getSize())));

	}

	@Query("eventOptions")
	@Description("Returns the list of available options for the event")
	public Uni<Collection<EventOption>> eventOptions(
		@Name("sortable") @DefaultValue("true") boolean sortable,
		@Name("sortType") @DefaultValue("ASC") SortType sortType,
		@Name("size") @DefaultValue("20") int size,
		@Name("from") @DefaultValue("0") int from
	) {

		String[] fields = _getFieldsFromContext();

		if (fields.length == 0) {
			return Uni.createFrom().item(List.of());
		}

		return Uni.createFrom().nothing();

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
	public Uni<List<Event>> getEvents(
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

		String[] fields = _getFieldsFromContext();

		if (fields.length == 0) {
			return Uni.createFrom().item(List.of());
		}
		return Uni
			.createFrom()
			.deferred(() -> {

				BoolQueryBuilder boolQueryBuilder =
					_createBoolQuery(
						id, type, className, groupKey, classPK, gte, lte);

				SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
				searchSourceBuilder.query(boolQueryBuilder);
				searchSourceBuilder.size(size);
				searchSourceBuilder.from(from);
				searchSourceBuilder.sort(sortBy.getColumn(), sortType.getSort());
				searchSourceBuilder.fetchSource(fields, null);

				return eventRepository
					.search(searchSourceBuilder);
			});

	}

	private BoolQueryBuilder _createBoolQuery(
		String id, String type, String className, String groupKey,
		String classPK, LocalDateTime gte, LocalDateTime lte) {
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		if (id != null && !id.isBlank()) {
			boolQueryBuilder.must(QueryBuilders.matchQuery(Event.ID, id));
		}

		if (type != null && !type.isBlank()) {
			boolQueryBuilder.must(QueryBuilders.matchQuery(Event.TYPE, type));
		}

		if (className != null && !className.isBlank()) {
			boolQueryBuilder.must(QueryBuilders.matchQuery(Event.CLASS_NAME,
				className));
		}

		if (groupKey != null && !groupKey.isBlank()) {
			boolQueryBuilder.must(QueryBuilders.matchQuery(Event.GROUP_KEY,
				groupKey));
		}

		if (classPK != null && !classPK.isBlank()) {
			boolQueryBuilder.must(QueryBuilders.matchQuery(Event.CLASS_PK,
				classPK));
		}

		if (gte != null || lte != null) {

			RangeQueryBuilder rangeQueryBuilder = QueryBuilders
				.rangeQuery(Event.CREATED);

			if (gte != null) {
				rangeQueryBuilder.gte(gte.toString());
			}

			if (lte != null) {
				rangeQueryBuilder.lte(lte.toString());
			}

			boolQueryBuilder.must(rangeQueryBuilder);

		}

		return boolQueryBuilder;
	}

	private String[] _getFieldsFromContext() {

		DataFetchingEnvironment dfe =
			context.unwrap(DataFetchingEnvironment.class);

		DataFetchingFieldSelectionSet selectionSet = dfe.getSelectionSet();

		return selectionSet
			.getFields()
			.stream()
			.map(SelectedField::getQualifiedName)
			.toArray(String[]::new);

	}

	@Inject
	SmallRyeContext context;

	@Inject
	EventRepository eventRepository;

	@Inject
	EventMapper mapper;

}
