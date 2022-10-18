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

package io.openk9.search.query.internal.query.parser.annotator;

import io.openk9.search.api.query.parser.CategorySemantics;
import io.openk9.search.client.api.RestHighLevelClientProvider;
import io.openk9.search.query.internal.query.parser.util.Utils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.phrase.DirectCandidateGeneratorBuilder;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestion;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseAutoCorrectAnnotator extends BaseAnnotator {

	private final Map<Long, List<String>> tenantKeywordsMap;

	public BaseAutoCorrectAnnotator(String...keywords) {
		this(List.of(keywords));
	}

	public BaseAutoCorrectAnnotator(List<String> keywords) {
		this.tenantKeywordsMap = _createTenantKeywordsMap(keywords);
	}

	@Override
	public List<CategorySemantics> annotate_(long tenantId, String...tokens) {

		List<String> normalizedKeywords =
			tenantKeywordsMap.getOrDefault(
				tenantId, tenantKeywordsMap.get(-1L));

		if (normalizedKeywords == null) {
			return List.of();
		}

		RestHighLevelClient restHighLevelClient =
			restHighLevelClientProvider.get();

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		String token;

		if (tokens.length == 1) {
			token = tokens[0];
			if (Utils.inQuote(token)) {
				return List.of(
					CategorySemantics.of(
						"$QUOTE_TOKEN",
						Map.of(
							"tokenType", "TEXT",
							"value", token,
							"score", 100.0f
						)
					)
				);
			}

		}
		else {
			token = String.join(" ", tokens);
		}

		for (String normalizedKeyword : normalizedKeywords) {

			PhraseSuggestionBuilder builder =
				SuggestBuilders.phraseSuggestion(
						normalizedKeyword)
					.addCandidateGenerator(
						new DirectCandidateGeneratorBuilder(
							normalizedKeyword)
							.suggestMode("always"))
					.text(token)
					.size(_annotatorConfig.autocorrectionSize())
					.maxErrors(2)
					.gramSize(3);

			SuggestBuilder suggestBuilder =
				new SuggestBuilder().addSuggestion("suggestion", builder);

			searchSourceBuilder.suggest(suggestBuilder);
		}

		SearchRequest searchRequest;

		if (tenantId == -1) {
			searchRequest = new SearchRequest(
				"*-*-data");
		}
		else {
			searchRequest = new SearchRequest(
				tenantId + "-*-data");
		}

		searchRequest.source(searchSourceBuilder);

		try {

			List<CategorySemantics> categorySemantics = new ArrayList<>();

			SearchResponse search =
				restHighLevelClient.search(
					searchRequest, RequestOptions.DEFAULT);

			for (Suggest.Suggestion<? extends Suggest.Suggestion.Entry<?
				extends Suggest.Suggestion.Entry.Option>> entries : search.getSuggest()) {

				_log.info("Entries: " + entries.toString());

				for (Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option> entry : entries) {

					for (Suggest.Suggestion.Entry.Option option : entry) {

						String text = option.getText().string();

						categorySemantics.add(
							CategorySemantics.of(
								"$AUTOCORRECT",
								Map.of(
									"tokenType", "AUTOCORRECT",
									"value", text,
									"score", 0.0f
								)
							)
						);

					}

				}

			}

			if (_log.isDebugEnabled()) {
				_log.debug(
					"for token {} found {} category semantics", token,
					categorySemantics);
			}

			return categorySemantics;
		}
		catch (IOException e) {
			_log.error(e.getMessage(), e);
		}

		return List.of();

	}

	private boolean _arrayContains(
		List<String> autocompleteEntityFields, String keyword) {
		return autocompleteEntityFields.contains(keyword);

	}

	private boolean _arrayContains(
		String[] autocompleteEntityFields, String keyword) {

		for (String autocompleteEntityField : autocompleteEntityFields) {
			if (keyword.equals(autocompleteEntityField)) {
				return true;
			}
		}

		return false;

	}

	private Map<Long, List<String>> _createTenantKeywordsMap(
		List<String> keywords) {

		if (keywords == null) {
			return Map.of();
		}

		Map<Long, List<String>> tenantKeywordsMap = new HashMap<>();

		for (String keyword : keywords) {
			long tenantId = -1;
			if (keyword.contains(";")) {
				String[] split = keyword.split(";");
				tenantId = Long.parseLong(split[0]);
				keyword = split[1];
			}

			List<String> value = tenantKeywordsMap.computeIfAbsent(
				tenantId, (k) -> new ArrayList<>());

			if (!_arrayContains(value, keyword)) {
				value.add(keyword);
			}

		}

		List<String> allTenantKeywords = tenantKeywordsMap.get(-1L);

		if (allTenantKeywords != null) {
			for (Map.Entry<Long, List<String>> e : tenantKeywordsMap.entrySet()) {
				if (e.getKey() == -1L) {
					continue;
				}
				List<String> value = e.getValue();
				for (String allTenantKeyword : allTenantKeywords) {
					if (!_arrayContains(value, allTenantKeyword)) {
						value.add(allTenantKeyword);
					}
				}
			}
		}

		return tenantKeywordsMap;

	}

	protected void setRestHighLevelClientProvider(
		RestHighLevelClientProvider restHighLevelClientProvider) {
		this.restHighLevelClientProvider = restHighLevelClientProvider;
	}

	protected RestHighLevelClientProvider restHighLevelClientProvider;

	private static final Logger _log = LoggerFactory.getLogger(
		BaseAutoCorrectAnnotator.class);

}