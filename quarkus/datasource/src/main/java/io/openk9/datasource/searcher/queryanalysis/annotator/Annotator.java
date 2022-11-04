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

import io.openk9.datasource.searcher.queryanalysis.CategorySemantics;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Set;

public interface Annotator extends Comparable<Annotator> {

	List<CategorySemantics> annotate(String...tokens);

	default List<CategorySemantics> annotate(
		Set<String> context, String...tokens) {
		return annotate(tokens);
	}

	default int weight() {
		return 1;
	}

	default int getLastTokenCount() {
		return -1;
	}

	Annotator DUMMY_ANNOTATOR = new Annotator() {
		@Override
		public List<CategorySemantics> annotate(String... tokens) {

			logger.warn("dummy annotator");

			return List.of();
		}

		@Override
		public int compareTo(Annotator o) {
			return 0;
		}

		private static final Logger logger = Logger.getLogger(
			Annotator.DUMMY_ANNOTATOR.getClass());

	};

}
