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

import io.openk9.search.api.query.parser.Annotator;
import io.openk9.search.api.query.parser.CategorySemantics;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public abstract class BaseAnnotator implements Annotator {

	public abstract List<CategorySemantics> annotate_(
		long tenantId, String...tokens);

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
		long tenantId, Set<String> context, String...tokens) {

		List<CategorySemantics> result = annotate_(tenantId, tokens);

		if (tokens.length == 1 && !result.isEmpty()) {

			String key = tokens[0];

			context.add(key);

		}

		return result;

	}

	@Override
	public List<CategorySemantics> annotate(
		long tenantId, String...tokens) {
		return annotate_(tenantId, tokens);
	}

	@Override
	public int compareTo(Annotator o) {
		return Integer.compare(this.weight(), o.weight());
	}

	@Override
	public int weight() {
		return 1;
	}

	protected void setAnnotatorConfig(AnnotatorConfig annotatorConfig) {
		_annotatorConfig = annotatorConfig;
		stopWords = List.of(annotatorConfig.stopWords());
	}

	protected AnnotatorConfig _annotatorConfig;
	protected List<String> stopWords;

}
