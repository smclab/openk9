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

import java.util.Arrays;
import java.util.Iterator;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

class CompositeSqlIdentifier implements SqlIdentifier {

	private final SqlIdentifier[] parts;

	CompositeSqlIdentifier(SqlIdentifier... parts) {
		this.parts = parts;
	}

	@Override
	public Iterator<SqlIdentifier> iterator() {
		return Arrays.asList(parts).iterator();
	}

	@Override
	public SqlIdentifier transform(
		UnaryOperator<String> transformationFunction) {
		throw new UnsupportedOperationException("Composite SQL Identifiers cannot be transformed");
	}

	@Override
	public String toSql(
		IdentifierProcessing processing) {

		StringJoiner stringJoiner = new StringJoiner(".");

		for (SqlIdentifier namePart : parts) {
			stringJoiner.add(namePart.toSql(processing));
		}

		return stringJoiner.toString();
	}

	@Override
	public String getReference(
		IdentifierProcessing processing) {
		throw new UnsupportedOperationException("Composite SQL Identifiers can't be used for reference name retrieval");
	}

	@Override
	public boolean equals(Object o) {

		if (this == o) {
			return true;
		}

		if (o instanceof SqlIdentifier) {
			return toString().equals(o.toString());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return toSql(IdentifierProcessing.ANSI);
	}
}
