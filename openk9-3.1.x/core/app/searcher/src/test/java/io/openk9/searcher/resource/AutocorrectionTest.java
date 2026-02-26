/*
 * Copyright (C) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
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

package io.openk9.searcher.resource;

import io.openk9.searcher.payload.response.AutocorrectionDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AutocorrectionTest {

	@Test
	void should_correct_one_word_phrase() {

		var originalText = "storria";
		var expectedText = "storia";
		var suggestions = List.of(
			new AutocorrectionDTO.Suggestion("storria", 0, 7, "storia")
		);

		Assertions.assertEquals(
			expectedText,
			SearchResource._generateAutocorrectionText(originalText, suggestions)
		);
	}

	@Test
	void should_correct_multiple_errors_phrase() {

		var originalText = "proptio una bella storria lunca";
		var expectedText = "proprio una bella storia lunga";
		var suggestions = List.of(
			new AutocorrectionDTO.Suggestion("proptio", 0, 7, "proprio"),
			new AutocorrectionDTO.Suggestion("storria", 18, 7, "storia"),
			new AutocorrectionDTO.Suggestion("lunca", 26, 5, "lunga")
		);

		Assertions.assertEquals(
			expectedText,
			SearchResource._generateAutocorrectionText(originalText, suggestions)
		);
	}

	@Test
	void should_correct_changing_size_phrase() {

		var originalText = "proio unu bella stoia lunc";
		var expectedText = "proprio una bella storia lunga";
		var suggestions = List.of(
			new AutocorrectionDTO.Suggestion("proio", 0, 5, "proprio"),
			new AutocorrectionDTO.Suggestion("unu", 6, 3, "una"),
			new AutocorrectionDTO.Suggestion("stoia", 16, 5, "storia"),
			new AutocorrectionDTO.Suggestion("lunc", 22, 4, "lunga")
		);

		Assertions.assertEquals(
			expectedText,
			SearchResource._generateAutocorrectionText(originalText, suggestions)
		);
	}

	@Test
	void should_return_original_text() {
		var originalText = "text example";

		Assertions.assertEquals(
			originalText,
			SearchResource._generateAutocorrectionText(originalText, null)
		);

		Assertions.assertEquals(
			originalText,
			SearchResource._generateAutocorrectionText(originalText, List.of())
		);
	}
}
