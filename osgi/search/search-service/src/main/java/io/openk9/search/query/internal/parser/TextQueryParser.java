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

package io.openk9.search.query.internal.parser;

import io.openk9.plugin.driver.manager.model.DocumentTypeDTO;
import io.openk9.plugin.driver.manager.model.FieldBoostDTO;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.plugin.driver.manager.model.SearchKeywordDTO;
import io.openk9.search.api.query.QueryParser;
import io.openk9.search.api.query.SearchToken;
import io.openk9.search.query.internal.query.parser.util.Utils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
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
@Designate(ocd = TextQueryParser.Config.class)
public class TextQueryParser implements QueryParser {

	@ObjectClassDefinition
	@interface Config {
		float boost() default 1.0f;
	}

	@Activate
	@Modified
	void activate(TextQueryParser.Config config) {
		_boost = config.boost();
	}

	@Override
	public Mono<Consumer<BoolQueryBuilder>> apply(Context context) {
		return Mono.fromSupplier(() -> {

			List<SearchToken> searchTokens = context
				.getTokenTypeGroup()
				.getOrDefault(TYPE, List.of());

			return bool -> _textEntityQuery(
				searchTokens, bool,
				context.getPluginDriverDocumentTypeList(),
				context.getQueryCondition());

		});
	}

	private void _textEntityQuery(
		List<SearchToken> tokenTextList, BoolQueryBuilder query,
		List<PluginDriverDTO> entityMapperList, QueryCondition queryCondition) {

		if (!tokenTextList.isEmpty()) {

			for (SearchToken searchToken : tokenTextList) {
				_termQueryPrefixValues(
					searchToken, query, entityMapperList, queryCondition);
			}

		}

	}

	private void _termQueryPrefixValues(
		SearchToken tokenText, BoolQueryBuilder query,
		List<PluginDriverDTO> entityMapperList, QueryCondition queryCondition) {

		String[] values = tokenText.getValues();

		if (values.length == 0) {
			return;
		}

		String keywordKey = tokenText.getKeywordKey();

		boolean keywordKeyIsPresent =
			keywordKey != null && !keywordKey.isBlank();

		Predicate<SearchKeywordDTO> keywordKeyPredicate;

		if (keywordKeyIsPresent) {
			keywordKeyPredicate = searchKeyword ->
				searchKeyword.getKeyword().equals(keywordKey);
		}
		else {
			keywordKeyPredicate = ignore -> true;
		}

		if (queryCondition == QueryCondition.DEFAULT) {
			queryCondition =
				tokenText.getFilter() != null && tokenText.getFilter()
					? QueryCondition.FILTER
					: QueryCondition.SHOULD;
		}

		Map<String, Float> keywordBoostMap =
			entityMapperList
				.stream()
				.map(PluginDriverDTO::getDocumentTypes)
				.flatMap(Collection::stream)
				.map(DocumentTypeDTO::getSearchKeywords)
				.flatMap(Collection::stream)
				.filter(SearchKeywordDTO::isText)
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

		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

		boolQueryBuilder.boost(_boost);

		for (String value : values) {

			boolean inQuote = Utils.inQuote(value);

			if (inQuote) {
				value = Utils.removeQuote(value);
			}

			int length = value.split("\\s+").length;

			if (!inQuote || length == 1) {

				MultiMatchQueryBuilder multiMatchQueryBuilder =
					new MultiMatchQueryBuilder(value);

				multiMatchQueryBuilder.fields(keywordBoostMap);

				boolQueryBuilder.should(multiMatchQueryBuilder);

			}

			if (length > 1) {

				MultiMatchQueryBuilder multiMatchQueryBuilder =
					new MultiMatchQueryBuilder(value);

				multiMatchQueryBuilder.fields(keywordBoostMap);

				multiMatchQueryBuilder.type(
					MultiMatchQueryBuilder.Type.PHRASE);

				if (!inQuote) {
					multiMatchQueryBuilder.slop(2);
				}

				multiMatchQueryBuilder.boost(2.0f);

				boolQueryBuilder.should(multiMatchQueryBuilder);

			}

		}

		queryCondition.accept(query, boolQueryBuilder);

	}

	private float _boost;

	private static final String TYPE = "TEXT";

}
