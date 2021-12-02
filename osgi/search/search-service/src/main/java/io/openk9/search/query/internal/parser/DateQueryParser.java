package io.openk9.search.query.internal.parser;

import io.openk9.plugin.driver.manager.model.DocumentTypeDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.plugin.driver.manager.model.SearchKeywordDTO;
import io.openk9.search.api.query.QueryParser;
import io.openk9.search.api.query.SearchToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.osgi.service.component.annotations.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(
	immediate = true,
	service = QueryParser.class
)
public class DateQueryParser implements QueryParser {

	@Override
	public Mono<Consumer<BoolQueryBuilder>> apply(Context context) {
		return Mono.fromSupplier(() -> {

			List<SearchToken> searchTokens = context
				.getTokenTypeGroup()
				.getOrDefault(TYPE, List.of());

			return bool -> _rangeQuery(
				searchTokens, context.getPluginDriverDocumentTypeList(), bool);

		});
	}

	private void _rangeQuery(
		List<SearchToken> searchTokens,
		List<PluginDriverDTO> pluginDriverDocumentTypeList,
		BoolQueryBuilder bool) {

		if (searchTokens.isEmpty()) {
			return;
		}

		List<SearchKeywordToken> collect =
			pluginDriverDocumentTypeList
				.stream()
				.map(PluginDriverDTO::getDocumentTypes)
				.flatMap(Collection::stream)
				.map(DocumentTypeDTO::getSearchKeywords)
				.flatMap(Collection::stream)
				.filter(SearchKeywordDTO::isDate)
				.flatMap(searchKeywordDTO -> {

					for (SearchToken searchToken : searchTokens) {
						if (searchToken.getKeywordKey().equals(
							searchKeywordDTO.getKeyword())) {
							return Stream.of(SearchKeywordToken.of(searchKeywordDTO,
								searchToken));
						}
					}

					return Stream.empty();

				})
				.collect(Collectors.toList());



		if (!collect.isEmpty()) {

			BoolQueryBuilder internalBool = QueryBuilders.boolQuery();

			boolean flag = false;

			for (SearchKeywordToken searchKeywordToken : collect) {

				SearchToken searchToken = searchKeywordToken.getSearchToken();

				String[] values = searchToken.getValues();

				String gte;
				String lte;

				if (values.length != 0) {

					RangeQueryBuilder rangeQueryBuilder =
						QueryBuilders.rangeQuery(searchToken.getKeywordKey());

					if (values.length == 1) {
						gte = values[0];
						rangeQueryBuilder.gte(gte);
					}
					else if (values.length == 2) {
						gte = values[0];
						lte = values[1];
						if (gte == null || gte.isBlank()) {
							rangeQueryBuilder.lte(lte);
						}
						else {
							rangeQueryBuilder
								.gte(gte)
								.lte(lte);
						}
					}

					flag = true;

					internalBool.should(rangeQueryBuilder);

				}

			}

			if (flag) {
				bool.must(internalBool);
			}

		}


	}

	private static final String TYPE = "DATE";

	@Data
	@Builder
	@AllArgsConstructor(staticName = "of")
	private static class SearchKeywordToken {
		private final SearchKeywordDTO searchKeyword;
		private final SearchToken searchToken;
	}

}
