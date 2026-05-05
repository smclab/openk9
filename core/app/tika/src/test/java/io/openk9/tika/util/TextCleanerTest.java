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

package io.openk9.tika.util;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("TextCleaner")
public class TextCleanerTest {

	private static final Logger log =  Logger.getLogger(TextCleanerTest.class);

	@Test
	@DisplayName("Return cleaned text")
	void returnCleanedText() {

		Assertions.assertEquals(
			"This is a simple sentence with non-breaking spaces.",
			TextCleaner.cleanText("This&nbsp;is a&nbsp;&nbsp;simple sentence with&nbsp;non-breaking spaces.")
		);

		Assertions.assertEquals(
			"This sentence starts and ends with non-breaking spaces.",
			TextCleaner.cleanText("&nbsp;&nbsp;This sentence starts and ends with non-breaking spaces.&nbsp;")
		);

		Assertions.assertEquals(
			"This sentence has too many spaces.",
			TextCleaner.cleanText("This    sentence     has      too many spaces.")
		);

		Assertions.assertEquals(
			"Special characters: & \" ' should be decoded.",
			TextCleaner.cleanText("Special characters: &amp; &quot; &#39; should be decoded.")
		);
	}

	@ParameterizedTest
	@ValueSource(
		strings = {
			"""
				<p>
				The Euro-
				pean Central Bank announced a new policy mea-
				sure aimed at stabilizing inflation across the euro-
				zone.&nbsp;According to officials, the inter-
				vention will take effect im-
				mediately.
				</p>
				
				<p>
				Experts say that the eco-
				nomic outlook remains uncer-
				tain, with several fac-
				tors contributing to mar-
				ket volatility.&nbsp;&nbsp;&nbsp;Investors are advised to main-
				tain a diversified port-
				folio.
				</p>
			"""
		}
	)
	@DisplayName("Return cleaned HTML text")
	void returnCleanedHtmlText(String rawText) {
		Assertions.assertDoesNotThrow(() ->
			log.info(TextCleaner.cleanText(rawText))
		);
	}
}
