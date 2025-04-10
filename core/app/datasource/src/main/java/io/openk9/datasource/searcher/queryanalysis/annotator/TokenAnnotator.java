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
import java.util.Set;

import io.openk9.datasource.model.Annotator;
import io.openk9.datasource.searcher.TenantWithBucket;
import io.openk9.datasource.searcher.queryanalysis.CategorySemantics;

class TokenAnnotator extends BaseAnnotator {

	public TokenAnnotator(
		TenantWithBucket tenantWithBucket, Annotator annotator, List<String> stopWords) {
		super(tenantWithBucket, annotator, stopWords);
	}

	@Override
	public List<CategorySemantics> annotate(
		Set<String> context, String... tokens) {

		if (tokens.length == 1) {
			String token = tokens[0];

			if (stopWords.contains(token)) {
				return List.of();
			}

			if (!context.contains(token)) {
				return List.of(
					CategorySemantics.of(
						"$TOKEN",
						Map.of(
							"tokenType", "TOKEN",
							"value", token,
							"score", 1.0f
						)
					)
				);
			}

		}

		return List.of();
	}

	@Override
	public List<CategorySemantics> annotate(String...tokens) {

		if (tokens.length == 1) {
			String token = tokens[0];
			return List.of(
				CategorySemantics.of(
					"$TOKEN",
					Map.of(
						"tokenType", "TOKEN",
						"value", token,
						"score", 1.0f
					)
				)
			);
		}

		return List.of();

	}

	@Override
	public int weight() {
		return 10;
	}

}
