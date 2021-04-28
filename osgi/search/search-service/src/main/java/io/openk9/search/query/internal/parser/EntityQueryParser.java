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

					String entityType = searchToken.getEntityType();

					String nestEntityPath = "entities." + entityType;

					String nestIdPath = nestEntityPath + ".id";

					BoolQueryBuilder innerNestBoolQuery = QueryBuilders
						.boolQuery()
						.must(_multiMatchValues(nestIdPath, ids));

					String keywordKey = searchToken.getKeywordKey();

					if (keywordKey != null && !keywordKey.isEmpty()) {
						innerNestBoolQuery
							.must(QueryBuilders.matchQuery(
								nestEntityPath + ".context", keywordKey));
					}

					bool.filter(
						QueryBuilders
							.nestedQuery(
								nestEntityPath,
								innerNestBoolQuery,
								ScoreMode.Max)
							.ignoreUnmapped(true)
					);

				}
			};

		});
	}

	private QueryBuilder _multiMatchValues(String field, String[] ids) {

		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

		for (String id : ids) {
			boolQuery.should(QueryBuilders.termQuery(field, id));
		}

		return boolQuery;

	}

	public static final String TYPE = "ENTITY";

}
