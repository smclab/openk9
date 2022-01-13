package io.openk9.search.query.internal.parser;

import io.openk9.plugin.driver.manager.model.DocumentTypeDTO;
import io.openk9.plugin.driver.manager.model.FieldBoostDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.plugin.driver.manager.model.SearchKeywordDTO;
import io.openk9.search.api.query.QueryParser;
import io.openk9.search.api.query.SearchToken;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.osgi.service.component.annotations.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component(
	immediate = true,
	service = QueryParser.class
)
public class SearchAsYouTypeQueryParser implements QueryParser {

	@Override
	public Mono<Consumer<BoolQueryBuilder>> apply(Context context) {
		return Mono.fromSupplier(() -> {

			List<SearchToken> searchTokens = context
				.getTokenTypeGroup()
				.getOrDefault(TYPE, List.of());

			return bool -> _searchAsYouTypeEntityQuery(
				searchTokens, bool,
				context.getPluginDriverDocumentTypeList());

		});
	}

	private void _searchAsYouTypeEntityQuery(
		List<SearchToken> tokenTextList, BoolQueryBuilder query,
		List<PluginDriverDTO> entityMapperList) {

		if (!tokenTextList.isEmpty()) {

			for (SearchToken searchToken : tokenTextList) {
				_termSearchAsYouTypeQueryValues(
					searchToken, query, entityMapperList);
			}

		}

	}

	private void _termSearchAsYouTypeQueryValues(
		SearchToken tokenText, BoolQueryBuilder query,
		List<PluginDriverDTO> entityMapperList) {

		String[] values = tokenText.getValues();

		if (values.length == 0) {
			return;
		}

		String keywordKey = tokenText.getKeywordKey();

		Predicate<SearchKeywordDTO> keywordKeyPredicate =
			searchKeyword -> keywordKey == null || keywordKey.isEmpty() ||
							 searchKeyword.getKeyword().equals(keywordKey);

		Map<String, Float> keywordBoostMap =
			entityMapperList
				.stream()
				.map(PluginDriverDTO::getDocumentTypes)
				.flatMap(Collection::stream)
				.map(DocumentTypeDTO::getSearchKeywords)
				.flatMap(Collection::stream)
				.filter(SearchKeywordDTO::isAutocomplete)
				.filter(keywordKeyPredicate)
				.map(SearchKeywordDTO::getFieldBoost)
				.collect(
					Collectors.toMap(
						FieldBoostDTO::getKeyword,
						FieldBoostDTO::getBoost,
						Math::max,
						HashMap::new
					)
				);

		for (String value : values) {

			MultiMatchQueryBuilder multiMatchQueryBuilder =
				new MultiMatchQueryBuilder(value);

			multiMatchQueryBuilder.type(
				MultiMatchQueryBuilder.Type.BOOL_PREFIX);

			multiMatchQueryBuilder.fields(keywordBoostMap);

			query.must(multiMatchQueryBuilder);

		}

	}

	private static final String TYPE = "AUTOCOMPLETE";

}
