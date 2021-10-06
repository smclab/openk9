package io.openk9.search.query.internal.parser;

import io.openk9.search.api.query.QueryParser;
import io.openk9.search.api.query.SearchToken;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.osgi.service.component.annotations.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;

@Component(
	immediate = true,
	service = QueryParser.class
)
public class DocTypeQueryParser implements QueryParser {

	@Override
	public Mono<Consumer<BoolQueryBuilder>> apply(Context context) {
		return Mono.fromSupplier(() -> {

			List<SearchToken> searchTokens = context
				.getTokenTypeGroup()
				.getOrDefault(TYPE, List.of());

			return (bool) -> _docTypeBoolQuery(searchTokens, bool);

		});
	}

	private void _docTypeBoolQuery(
		List<SearchToken> searchTokenList, BoolQueryBuilder bool) {

		if (searchTokenList.isEmpty()) {
			return;
		}

		String[][] typeArray =
			searchTokenList
				.stream()
				.map(SearchToken::getValues)
				.toArray(String[][]::new);

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		for (String[] types : typeArray) {

			BoolQueryBuilder shouldBool = QueryBuilders.boolQuery();

			for (String type : types) {
				shouldBool
					.should(
						QueryBuilders
							.matchQuery("documentTypes", type)
							.operator(Operator.AND)
					);
			}

			boolQueryBuilder.must(shouldBool);

		}

		bool.filter(boolQueryBuilder);

	}

	public static final String TYPE = "DOCTYPE";
}
