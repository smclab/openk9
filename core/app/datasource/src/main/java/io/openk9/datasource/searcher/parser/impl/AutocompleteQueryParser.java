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

package io.openk9.datasource.searcher.parser.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.QueryParserType;
import io.openk9.datasource.searcher.parser.ParserContext;
import io.openk9.datasource.searcher.parser.QueryParser;
import io.openk9.datasource.searcher.util.Utils;
import io.openk9.searcher.client.dto.ParserSearchToken;

import io.smallrye.mutiny.Uni;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.MultiMatchQueryBuilder;
import org.opensearch.index.query.QueryBuilders;

@ApplicationScoped
@Named("AutocompleteQueryParser")
public class AutocompleteQueryParser implements QueryParser {

	@Override
	public QueryParserType getType() {
		return QueryParserType.AUTOCOMPLETE;
	}

	@Override
	public Uni<Void> apply(ParserContext parserContext) {

		BoolQueryBuilder mutableQuery = parserContext.getMutableQuery();

		List<ParserSearchToken> tokenTypeGroup =
			parserContext.getTokenTypeGroup();

		Bucket bucket = parserContext.getTenantWithBucket().getBucket();

		for (ParserSearchToken searchToken : tokenTypeGroup) {
			_termSearchAsYouTypeQueryValues(
				searchToken, mutableQuery, bucket.getDatasources());
		}

		return Uni.createFrom().voidItem();
	}

	private void _termSearchAsYouTypeQueryValues(
		ParserSearchToken tokenText, BoolQueryBuilder query,
		Set<Datasource> datasources) {

		List<String> values = tokenText.getValues();

		if (values.isEmpty()) {
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

}
