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


public interface IdentifierProcessing {

	IdentifierProcessing ANSI = create(
		IdentifierProcessing.Quoting.ANSI, IdentifierProcessing.LetterCasing.UPPER_CASE);

	IdentifierProcessing NONE = create(
		IdentifierProcessing.Quoting.NONE, IdentifierProcessing.LetterCasing.AS_IS);

	static DefaultIdentifierProcessing create(
		IdentifierProcessing.Quoting quoting, IdentifierProcessing.LetterCasing letterCasing) {
		return new DefaultIdentifierProcessing(quoting, letterCasing);
	}

	String quote(String identifier);

	String standardizeLetterCase(String identifier);

	class Quoting {

		public static final IdentifierProcessing.Quoting
			ANSI = new IdentifierProcessing.Quoting("\"");

		public static final IdentifierProcessing.Quoting
			NONE = new IdentifierProcessing.Quoting("");

		private final String prefix;
		private final String suffix;

		public Quoting(String prefix, String suffix) {

			this.prefix = prefix;
			this.suffix = suffix;
		}

		public Quoting(String quoteCharacter) {
			this(quoteCharacter, quoteCharacter);
		}

		public String apply(String identifier) {
			return prefix + identifier + suffix;
		}
	}

	enum LetterCasing {

		UPPER_CASE {

			@Override
			public String apply(String identifier) {
				return identifier.toUpperCase();
			}
		},

		LOWER_CASE {

			@Override
			public String apply(String identifier) {
				return identifier.toLowerCase();
			}
		},

		AS_IS {

			@Override
			public String apply(String identifier) {
				return identifier;
			}
		};

		abstract String apply(String identifier);
	}
}
