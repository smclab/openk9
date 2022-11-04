package io.openk9.datasource.searcher.parser.impl;

import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.Tenant;
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
import java.util.function.Predicate;
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

		Tenant currentTenant = parserContext.getCurrentTenant();

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

		Predicate<DocTypeField> keywordKeyPredicate =
			searchKeyword -> keywordKey == null || keywordKey.isEmpty() ||
							 searchKeyword.getName().equals(keywordKey);

		Map<String, Float> keywordBoostMap =
			Utils.getDocTypeFieldsFrom(datasources)
				.filter(DocTypeField::getSearchable)
				.filter(DocTypeField::isAutocomplete)
				.filter(keywordKeyPredicate)
				.collect(
					Collectors.toMap(
						DocTypeField::getName,
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
