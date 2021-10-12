package io.openk9.search.query.internal.parser;

import io.openk9.search.api.query.QueryParser;
import io.openk9.search.api.query.SearchToken;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.osgi.service.component.annotations.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;

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

				for (SearchToken searchToken : searchTokens) {
					String[] ids = searchToken.getValues();

					BoolQueryBuilder boolQueryBuilder = QueryBuilders
						.boolQuery()
						.must(_multiMatchValues(ids));

					String entityType = searchToken.getEntityType();

					if (entityType != null && !entityType.isEmpty()) {
						boolQueryBuilder
							.must(
								QueryBuilders.nestedQuery(
									ENTITIES,
									QueryBuilders
										.matchQuery(
											ENTITIES_ENTITY_TYPE, entityType
										),
									ScoreMode.Max
								)
							);
					}

					String keywordKey = searchToken.getKeywordKey();

					if (keywordKey != null && !keywordKey.isEmpty()) {
						boolQueryBuilder
							.must(
								QueryBuilders.nestedQuery(
									ENTITIES,
									QueryBuilders.matchQuery(
										ENTITIES_CONTEXT, keywordKey),
									ScoreMode.Max
								)
							);
					}

					bool.filter(boolQueryBuilder);

				}
			};

		});
	}

	private QueryBuilder _multiMatchValues(String[] ids) {

		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

		for (String id : ids) {
			boolQuery.should(
				QueryBuilders.nestedQuery(
					ENTITIES,
					QueryBuilders.matchQuery(ENTITIES_ID, id),
					ScoreMode.Max
				)
			);
		}

		return boolQuery;

	}

	public static final String TYPE = "ENTITY";
	public static final String ENTITIES = "entities";
	public static final String ENTITIES_ID = "entities.id";
	public static final String ENTITIES_ENTITY_TYPE = "entities.entityType";
	public static final String ENTITIES_CONTEXT = "entities.context";

}
