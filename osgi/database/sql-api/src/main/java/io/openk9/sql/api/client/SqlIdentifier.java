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

package io.openk9.sql.api.client;

import java.util.Collections;
import java.util.Iterator;
import java.util.function.UnaryOperator;

public interface SqlIdentifier extends Iterable<SqlIdentifier> {
	SqlIdentifier
		EMPTY = new SqlIdentifier() {

		@Override
		public Iterator<SqlIdentifier> iterator() {
			return Collections.emptyIterator();
		}

		@Override
		public SqlIdentifier transform(
			UnaryOperator<String> transformationFunction) {
			return this;
		}

		@Override
		public String toSql(IdentifierProcessing processing) {
			throw new UnsupportedOperationException("An empty SqlIdentifier can't be used in to create SQL snippets");
		}

		@Override
		public String getReference(IdentifierProcessing processing) {
			throw new UnsupportedOperationException("An empty SqlIdentifier can't be used in to create column names");
		}

		public String toString() {
			return "<NULL-IDENTIFIER>";
		}
	};

	String getReference(IdentifierProcessing processing);

	default String getReference() {
		return getReference(IdentifierProcessing.NONE);
	}

	String toSql(IdentifierProcessing processing);

	SqlIdentifier transform(UnaryOperator<String> transformationFunction);

	static SqlIdentifier quoted(String name) {
		return new DefaultSqlIdentifier(name, true);
	}

	/**
	 * Create a new unquoted identifier given {@code name}.
	 *
	 * @param name the identifier.
	 * @return a new unquoted identifier given {@code name}.
	 */
	static SqlIdentifier unquoted(String name) {
		return new DefaultSqlIdentifier(name, false);
	}

	/**
	 * Create a new composite {@link SqlIdentifier} from one or more {@link SqlIdentifier}s.
	 * <p>
	 * Composite identifiers do not allow {@link #transform(UnaryOperator)} transformation.
	 * </p>
	 *
	 * @param sqlIdentifiers the elements of the new identifier.
	 * @return the new composite identifier.
	 */
	static SqlIdentifier from(
		SqlIdentifier... sqlIdentifiers) {
		return new CompositeSqlIdentifier(sqlIdentifiers);
	}
}
