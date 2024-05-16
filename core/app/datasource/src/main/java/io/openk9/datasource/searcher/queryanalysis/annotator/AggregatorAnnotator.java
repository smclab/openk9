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
import io.openk9.datasource.searcher.queryanalysis.CategorySemantics;
import org.opensearch.client.RestHighLevelClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AggregatorAnnotator extends BaseAggregatorAnnotator {

	public AggregatorAnnotator(
		String keyword,
		Bucket bucket,
		io.openk9.datasource.model.Annotator annotator,
		List<String> stopWords, RestHighLevelClient restHighLevelClient, JWT jwt) {
		super(bucket, annotator, stopWords, restHighLevelClient, null, jwt,
			keyword);
	}

	@Override
	protected CategorySemantics _createCategorySemantics(
		String aggregatorName, String aggregatorKey, String fieldName) {

		Map<String, Object> semantics = new HashMap<>(5);

		if (false) { // TODO
			semantics.put("keywordKey", aggregatorName);
		}

		String label;

		if (annotator.getDocTypeField().getParentDocTypeField() == null) {
			label = annotator.getDocTypeField().getName();
		}
		else {
			label = annotator.getDocTypeField().getParentDocTypeField().getName();
		}

		semantics.put("tokenType", "TEXT");
		semantics.put("label", label);
		semantics.put("keywordKey", aggregatorName);
		semantics.put("value", aggregatorKey);
		semantics.put("score", 50.0f);
		semantics.put("extra", annotator.getExtraParams());

		return CategorySemantics.of(
			"$AGGREGATE", Collections.unmodifiableMap(semantics));

	}

}