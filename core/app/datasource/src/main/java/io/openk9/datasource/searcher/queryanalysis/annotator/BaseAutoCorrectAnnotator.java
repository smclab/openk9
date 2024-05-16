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

package io.openk9.datasource.searcher.queryanalysis.annotator;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.searcher.queryanalysis.CategorySemantics;
import org.jboss.logging.Logger;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.suggest.Suggest;
import org.opensearch.search.suggest.SuggestBuilder;
import org.opensearch.search.suggest.SuggestBuilders;
import org.opensearch.search.suggest.phrase.DirectCandidateGeneratorBuilder;
import org.opensearch.search.suggest.phrase.PhraseSuggestionBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseAutoCorrectAnnotator extends BaseAnnotator {

	public BaseAutoCorrectAnnotator(
		Bucket bucket,
		io.openk9.datasource.model.Annotator annotator,
		List<String> stopWords,
		RestHighLevelClient restHighLevelClient,
		String includeField, String searchKeyword) {
		super(bucket, annotator, stopWords, null);
		this.includeField = includeField;
		this.searchKeyword = searchKeyword;
		this.restHighLevelClient = restHighLevelClient;
	}

	@Override
	public List<CategorySemantics> annotate(String...tokens) {

		String token = String.join(" ", tokens);
		
		PhraseSuggestionBuilder builder =
			SuggestBuilders.phraseSuggestion(searchKeyword)
				.addCandidateGenerator(
					new DirectCandidateGeneratorBuilder(searchKeyword)
						.suggestMode("always"))
				.text(token)
				.size(annotator.getSize())
				.maxErrors(2)
				.gramSize(3);

		SuggestBuilder suggestBuilder =
			new SuggestBuilder().addSuggestion("suggestion", builder);

		String[] indexNames =
			bucket
				.getDatasources()
				.stream()
				.map(Datasource::getDataIndex)
				.map(DataIndex::getName)
				.toArray(String[]::new);

		SearchRequest searchRequest = new SearchRequest(indexNames);

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		searchSourceBuilder.suggest(suggestBuilder);

		searchRequest.source(searchSourceBuilder);

		try {

			List<CategorySemantics> categorySemantics = new ArrayList<>();

			SearchResponse search =
				restHighLevelClient.search(
					searchRequest, RequestOptions.DEFAULT);

			for (Suggest.Suggestion<? extends Suggest.Suggestion.Entry<?
				extends Suggest.Suggestion.Entry.Option>> entries : search.getSuggest()) {

				for (Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option> entry : entries) {

					for (Suggest.Suggestion.Entry.Option option : entry) {

						String text = option.getText().string();

						if (!text.startsWith(token)) {
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

			}

			if (_log.isDebugEnabled()) {
				_log.debug(
					"for token " + token + " found " + categorySemantics + " category semantics");
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

	protected final RestHighLevelClient restHighLevelClient;

	protected final String searchKeyword;

	protected final String includeField;

	private static final Logger _log = Logger.getLogger(
		BaseAutoCorrectAnnotator.class);

}