package io.openk9.datasource.searcher.parser.impl;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;
import io.openk9.datasource.searcher.util.Utils;
import io.openk9.searcher.client.dto.ParserSearchToken;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class SearchAsYouTypeQueryParser implements QueryParser {

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void accept(ParserContext parserContext) {

		BoolQueryBuilder mutableQuery = parserContext.getMutableQuery();

		List<ParserSearchToken> tokenTypeGroup =
			parserContext.getTokenTypeGroup();

		Bucket currentTenant = parserContext.getCurrentTenant();

		for (ParserSearchToken searchToken : tokenTypeGroup) {
			_termSearchAsYouTypeQueryValues(
				searchToken, mutableQuery, currentTenant.getDatasources());
		}

	}

	private void _termSearchAsYouTypeQueryValues(
		ParserSearchToken tokenText, BoolQueryBuilder query,
		Set<Datasource> datasources) {

		List<String> values = tokenText.getValues();

		if (values.size() == 0) {
			return;
		}

		String keywordKey = tokenText.getKeywordKey();

		Map<String, Float> keywordBoostMap =
			Utils.getDocTypeFieldsFrom(datasources)
				.filter(
					searchKeyword ->
						searchKeyword.isSearchableAndAutocomplete() &&
						(
							keywordKey == null ||
							keywordKey.isEmpty() ||
							searchKeyword.getPath().equals(keywordKey)
						)
				)
				.collect(
					Collectors.toMap(
						DocTypeField::getPath,
						DocTypeField::getFloatBoost,
						Math::max,
						HashMap::new
					)
				);

		BoolQueryBuilder innerBoolQueryBuilder = QueryBuilders.boolQuery();

		for (String value : values) {

			MultiMatchQueryBuilder multiMatchQueryBuilder =
				new MultiMatchQueryBuilder(value);

			multiMatchQueryBuilder.type(
				MultiMatchQueryBuilder.Type.BOOL_PREFIX);

			multiMatchQueryBuilder.fields(keywordBoostMap);

			innerBoolQueryBuilder.should(multiMatchQueryBuilder);

		}

		query.must(innerBoolQueryBuilder);

	}

	private static final String TYPE = "AUTOCOMPLETE";

}
