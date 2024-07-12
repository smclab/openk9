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

import com.jayway.jsonpath.JsonPath;
import io.openk9.datasource.searcher.parser.ParserContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch._types.query_dsl.KnnQuery;
import org.opensearch.index.query.BoolQueryBuilder;

import java.util.Base64;
import java.util.List;

class KnnQueryParserTest {

	private static final int VECTOR_SIZE = 1800;
	private static final int K_NEIGHBORS = 2;

	@Test
	void addsKnnQuery() {

		var parserContext = new ParserContext();

		var boolQueryBuilder = new BoolQueryBuilder();

		parserContext.setMutableQuery(boolQueryBuilder);

		var knnQueryBuilder = new KnnQuery.Builder()
			.field("vector")
			.k(K_NEIGHBORS)
			.vector(randomVector(VECTOR_SIZE))
			.build()
			.toQuery();

		KnnQueryParser.addsKnnQuery(parserContext, knnQueryBuilder);

		var query = parserContext.getMutableQuery().toString();

		String wrappedQuery = JsonPath.parse(query).read("$.bool.must[0].wrapper.query");

		var wrappedKnnQuery = new String(Base64.getDecoder().decode(wrappedQuery));

		var documentContext = JsonPath.parse(wrappedKnnQuery);

		int k = documentContext.read("$.knn.vector.k");
		List<Float> vector = documentContext.read("$.knn.vector.vector");

		Assertions.assertEquals(K_NEIGHBORS, k);
		Assertions.assertEquals(VECTOR_SIZE, vector.size());

	}

	private float[] randomVector(int size) {
		var vector = new float[size];

		for (int i = 0; i < size; i++) {

			vector[i] = (float) Math.random();

		}

		return vector;
	}

}