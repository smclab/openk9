package io.openk9.search.query.internal.parser;

import io.openk9.search.api.query.QueryParser;
import io.openk9.search.api.query.SearchToken;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.osgi.service.component.annotations.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component(
	immediate = true,
	service = QueryParser.class
)
public class EntityQueryParser implements QueryParser {

	@Override
	public Mono<Consumer<BoolQueryBuilder>> apply(Context context) {

		return Mono.fromSupplier(() -> {

			List<SearchToken> searchTokens = context
				.getTokenTypeGroup()
				.getOrDefault(TYPE, List.of());

			return bool -> {

				Map<String, List<SearchToken>> searchTokenGroupingByType =
					searchTokens
						.stream()
						.collect(Collectors.groupingBy(
								searchToken ->
									searchToken.getEntityType() == null
										? ""
										: searchToken.getEntityType()
							)
						);

				BoolQueryBuilder outerBoolQueryBuilder =
					QueryBuilders.boolQuery();

				for (Map.Entry<String, List<SearchToken>> groupSearchTokens : searchTokenGroupingByType.entrySet()) {

					BoolQueryBuilder innerBoolQueryBuilder =
						QueryBuilders.boolQuery();

					String type = groupSearchTokens.getKey();

					if (!type.isBlank()) {
						innerBoolQueryBuilder
							.must(QueryBuilders.matchQuery(
								ENTITIES_ENTITY_TYPE, type));
					}

					for (SearchToken searchToken : groupSearchTokens.getValue()) {

						String[] ids = searchToken.getValues();

						BoolQueryBuilder boolQueryBuilder =
							innerBoolQueryBuilder
								.should(_multiMatchValues(ids));

						String keywordKey = searchToken.getKeywordKey();

						if (keywordKey != null && !keywordKey.isEmpty()) {
							boolQueryBuilder
								.should(QueryBuilders.matchQuery(
									ENTITIES_CONTEXT, keywordKey));
						}

					}

					outerBoolQueryBuilder.must(innerBoolQueryBuilder);

				}

				bool.filter(outerBoolQueryBuilder);

			};

		});
	}

	private QueryBuilder _multiMatchValues(String[] ids) {

		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

		for (String id : ids) {
			boolQuery.should(QueryBuilders.matchQuery(ENTITIES_ID, id));
		}

		return boolQuery;

	}

	public static final String TYPE = "ENTITY";
	public static final String ENTITIES_ID = "entities.id";
	public static final String ENTITIES_ENTITY_TYPE = "entities.entityType";
	public static final String ENTITIES_CONTEXT = "entities.context";

}
