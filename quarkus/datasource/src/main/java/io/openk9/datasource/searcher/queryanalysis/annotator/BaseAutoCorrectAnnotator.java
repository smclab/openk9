package io.openk9.datasource.searcher.queryanalysis.annotator;

import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.searcher.queryanalysis.CategorySemantics;
import io.openk9.datasource.searcher.util.Utils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.phrase.DirectCandidateGeneratorBuilder;
import org.elasticsearch.search.suggest.phrase.PhraseSuggestionBuilder;
import org.jboss.logging.Logger;

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