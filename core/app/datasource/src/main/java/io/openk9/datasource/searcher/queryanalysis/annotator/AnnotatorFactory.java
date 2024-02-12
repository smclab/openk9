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
import io.openk9.datasource.model.util.JWT;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class AnnotatorFactory {

	public Annotator getAnnotator(
		String schemaName, Bucket bucket,
		io.openk9.datasource.model.Annotator annotator,
		List<String> stopWords, JWT jwt) {

		return switch (annotator.getType()) {
			case TOKEN -> new TokenAnnotator(bucket, annotator, stopWords);
			case KEYWORD -> new KeywordAnnotator(bucket, annotator, stopWords);
			case STOPWORD -> new StopWordsAnnotator(bucket, annotator, stopWords);
			case NER -> new BaseNerAnnotator(
				bucket, annotator, stopWords, annotator.getFieldName(), client, schemaName);
			case DOCTYPE -> new DocTypeAnnotator(
				bucket, annotator, stopWords, client, jwt);
			case AGGREGATOR -> new AggregatorAnnotator(
				annotator.getDocTypeField().getPath(),
				bucket, annotator, stopWords, client, jwt);
			case AUTOCOMPLETE -> new BaseAutoCompleteAnnotator(
				bucket, annotator, stopWords, client,
				annotator.getFieldName(),
				annotator.getDocTypeField().getPath(), jwt);
			case KEYWORD_AUTOCOMPLETE -> new BaseKeywordAutoCompleteAnnotator(
				bucket, annotator, stopWords, client,
				annotator.getFieldName(),
				annotator.getDocTypeField().getPath(), jwt);
			case NER_AUTOCOMPLETE -> new BaseAutoCompleteNerAnnotator(
				bucket, annotator, stopWords, annotator.getFieldName(), client, schemaName);
			case AUTOCORRECT -> new BaseAutoCorrectAnnotator(
				bucket, annotator, stopWords, client,
				annotator.getFieldName(),
				annotator.getDocTypeField().getPath());
		};
	}

	@Inject
	RestHighLevelClient client;

}
