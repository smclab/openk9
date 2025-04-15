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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import io.openk9.datasource.searcher.model.TenantWithBucket;
import io.openk9.datasource.searcher.queryanalysis.CategorySemantics;

import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;

abstract class BaseAnnotator implements Annotator {

	protected final TenantWithBucket tenantWithBucket;

	protected QueryBuilder query(String field, String token) {
		return QueryBuilders.fuzzyQuery(field, token);
	}

	protected boolean _containsStopword(String[] tokens) {

		if (Arrays.stream(tokens).allMatch(stopWords::contains)) {
			return true;
		}

		int length = tokens.length;

		if (length > 1) {

			return stopWords.contains(tokens[0]) ||
				   stopWords.contains(tokens[length - 1]);

		}

		return false;

	}


	@Override
	public List<CategorySemantics> annotate(
		Set<String> context, String...tokens) {

		List<CategorySemantics> result = annotate(tokens);

		if (tokens.length == 1 && !result.isEmpty()) {

			String key = tokens[0];

			context.add(key);

		}

		return result;

	}

	@Override
	public int compareTo(Annotator o) {
		return Integer.compare(this.weight(), o.weight());
	}

	public BaseAnnotator(
		TenantWithBucket tenantWithBucket,
		io.openk9.datasource.model.Annotator annotator,
		List<String> stopWords) {

		this.tenantWithBucket = tenantWithBucket;
		this.annotator = annotator;
		this.stopWords = stopWords;

	}

	protected final io.openk9.datasource.model.Annotator annotator;

	protected final List<String> stopWords;

}