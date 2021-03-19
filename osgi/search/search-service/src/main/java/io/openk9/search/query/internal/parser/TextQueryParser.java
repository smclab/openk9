package io.openk9.search.query.internal.parser;

import io.openk9.ingestion.driver.manager.api.DocumentType;
import io.openk9.ingestion.driver.manager.api.PluginDriver;
import io.openk9.ingestion.driver.manager.api.SearchKeyword;
import io.openk9.search.api.query.QueryParser;
import io.openk9.search.api.query.SearchToken;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.osgi.service.component.annotations.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component(
	immediate = true,
	service = QueryParser.class
)
public class TextQueryParser implements QueryParser {

	@Override
	public Mono<Consumer<BoolQueryBuilder>> apply(Context context) {
		return Mono.fromSupplier(() -> {

			List<SearchToken> searchTokens = context
				.getTokenTypeGroup()
				.getOrDefault(TYPE, List.of());

			return bool -> _textEntityQuery(
				searchTokens, bool,
				context.getPluginDriverDocumentTypeList());

		});
	}

	private void _textEntityQuery(
		List<SearchToken> tokenTextList, BoolQueryBuilder query,
		List<Map.Entry<PluginDriver, List<DocumentType>>> entityMapperList) {

		for (SearchToken searchToken : tokenTextList) {
			_termQueryPrefixValues(searchToken, query, entityMapperList);
		}
	}

	private void _termQueryPrefixValues(
		SearchToken tokenText, BoolQueryBuilder query,
		List<Map.Entry<PluginDriver, List<DocumentType>>> entityMapperList) {

		String[] values = tokenText.getValues();

		if (values.length == 0) {
			return;
		}

		String keywordKey = tokenText.getKeywordKey();

		Predicate<SearchKeyword> keywordKeyPredicate =
			searchKeyword -> keywordKey == null || keywordKey.isEmpty() ||
							 searchKeyword.getKeyword().equals(keywordKey);

		Map<String, Float> keywordBoostMap =
			entityMapperList
				.stream()
				.map(Map.Entry::getValue)
				.flatMap(Collection::stream)
				.map(DocumentType::getSearchKeywords)
				.flatMap(Collection::stream)
				.filter(SearchKeyword::isText)
				.distinct()
				.filter(keywordKeyPredicate)
				.map(SearchKeyword::getFieldBoost)
				.collect(
					Collectors.toMap(
						Map.Entry::getKey, Map.Entry::getValue));


		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		for (String value : values) {

			MultiMatchQueryBuilder multiMatchQueryBuilder =
				new MultiMatchQueryBuilder(value);

			multiMatchQueryBuilder.fields(keywordBoostMap);

			boolQueryBuilder.should(multiMatchQueryBuilder);

			multiMatchQueryBuilder =
				new MultiMatchQueryBuilder(value);

			multiMatchQueryBuilder.fields(keywordBoostMap);

			multiMatchQueryBuilder.type(
				MultiMatchQueryBuilder.Type.PHRASE);

			multiMatchQueryBuilder.slop(2);

			multiMatchQueryBuilder.boost(2.0f);

			boolQueryBuilder.should(multiMatchQueryBuilder);

		}

		query.must(boolQueryBuilder);


	}

	private static final String TYPE = "TEXT";

}
