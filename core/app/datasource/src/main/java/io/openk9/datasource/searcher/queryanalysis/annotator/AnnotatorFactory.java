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

import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.datasource.searcher.model.TenantWithBucket;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.opensearch.client.RestHighLevelClient;

@ApplicationScoped
public class AnnotatorFactory {

	public Annotator getAnnotator(
		TenantWithBucket tenantWithBucket,
		io.openk9.datasource.model.Annotator annotator,
		List<String> stopWords, JsonWebToken jwt) {

		return switch (annotator.getType()) {
			case TOKEN -> new TokenAnnotator(tenantWithBucket, annotator, stopWords);
			case KEYWORD -> new KeywordAnnotator(tenantWithBucket, annotator, stopWords);
			case STOPWORD -> new StopWordsAnnotator(tenantWithBucket, annotator, stopWords);
			case NER -> new BaseNerAnnotator(
				tenantWithBucket, annotator, stopWords, annotator.getFieldName(), client);
			case DOCTYPE -> new DocTypeAnnotator(
				tenantWithBucket, annotator, stopWords, client, jwt);
			case AGGREGATOR -> new AggregatorAnnotator(
				annotator.getDocTypeField().getPath(),
				tenantWithBucket, annotator, stopWords, client, jwt
			);
			case AUTOCOMPLETE -> new BaseAutoCompleteAnnotator(
				tenantWithBucket, annotator, stopWords, client,
				annotator.getFieldName(),
				annotator.getDocTypeField().getPath(), jwt);
			case KEYWORD_AUTOCOMPLETE -> new BaseKeywordAutoCompleteAnnotator(
				tenantWithBucket, annotator, stopWords, client,
				annotator.getFieldName(),
				annotator.getDocTypeField().getPath(), jwt);
			case NER_AUTOCOMPLETE -> new BaseAutoCompleteNerAnnotator(
				tenantWithBucket, annotator, stopWords, annotator.getFieldName(), client);
			case AUTOCORRECT -> new BaseAutoCorrectAnnotator(
				tenantWithBucket, annotator, stopWords, client,
				annotator.getFieldName(),
				annotator.getDocTypeField().getPath());
		};
	}

	@Inject
	RestHighLevelClient client;

}
