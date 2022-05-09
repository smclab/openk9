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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AggregatorAnnotator extends BaseAggregatorAnnotator {

	public AggregatorAnnotator(
		String keyword,
		AnnotatorConfig annotatorConfig,
		RestHighLevelClientProvider restHighLevelClientProvider) {
		super(keyword);
		super.setAnnotatorConfig(annotatorConfig);
		super.setRestHighLevelClientProvider(restHighLevelClientProvider);
	}

	@Override
	protected CategorySemantics _createCategorySemantics(
		String aggregatorName, String aggregatorKey) {

		Map<String, Object> semantics = new HashMap<>(5);

		if (_annotatorConfig.aggregatorKeywordKeyEnable()) {
			semantics.put("keywordKey", aggregatorName);
		}

		semantics.put("tokenType", "TEXT");
		semantics.put("keywordName", aggregatorName);
		semantics.put("value", aggregatorKey);
		semantics.put("score", 50.0f);

		return CategorySemantics.of(
			"$AGGREGATE", Collections.unmodifiableMap(semantics));

	}

}
