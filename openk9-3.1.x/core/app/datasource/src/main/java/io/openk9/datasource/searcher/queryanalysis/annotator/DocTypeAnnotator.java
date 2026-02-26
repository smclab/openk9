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
import java.util.Map;

import io.openk9.datasource.model.Annotator;
import io.openk9.datasource.searcher.model.TenantWithBucket;
import io.openk9.datasource.searcher.queryanalysis.CategorySemantics;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.opensearch.client.RestHighLevelClient;

public class DocTypeAnnotator extends BaseAggregatorAnnotator {

	public DocTypeAnnotator(
		TenantWithBucket tenantWithBucket,
		Annotator annotator,
		List<String> stopWords,
		RestHighLevelClient restHighLevelClient,
		JsonWebToken jwt) {

		super(
			tenantWithBucket, annotator, stopWords, restHighLevelClient, jwt,
			"documentTypes.keyword");
	}

	@Override
	protected CategorySemantics _createCategorySemantics(
		String aggregatorName, String aggregatorKey, String fieldName) {

		return CategorySemantics.of(
			"$DOCTYPE",
			Map.of(
				"label", "Doctype",
				"tokenType", "DOCTYPE",
				"value", aggregatorKey,
				"score", 50.0f
			)
		);

	}


}