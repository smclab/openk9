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

package io.openk9.datasource.model.dto.base;

import io.openk9.datasource.model.SortType;
import io.openk9.datasource.model.SuggestMode;
import io.smallrye.graphql.api.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.graphql.Description;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AutocorrectionDTO extends K9EntityDTO{

	@Nullable
	@Description("""
		The field used in the autocorrection query,
		must be a valid docTypeFieldId.
	""")
	private Long autocorrectionDocTypeFieldId;

	@Nullable
	@Description("""
		Specifies how suggestions should be ordered in the response. Valid values are:
		- 'score' (Default): Orders by similarity score (edit distance), then by document frequency, and finally by the term itself.
		- 'frequency': Orders by document frequency, then by similarity score, and finally by the term itself.
	""")
	private SortType sort;

	@Nullable
	@Description("""
		Controls for which terms suggestions should be included in the response. Valid values are:
		- 'missing' (Default): Returns suggestions only for input terms not found in the index.
		- 'popular': Returns suggestions only if the suggested terms appear more frequently in documents than the original query term.
		- 'always': Always returns matching suggestions for every term in the input text, regardless of its presence or frequency in the index.
	""")
	private SuggestMode suggestMode;

	@Nullable
	@Description("""
		An integer specifying the minimum length the matching prefix must have to start returning suggestions.
		For example, if 'prefix_length' is 3, an input of "ap" will not generate suggestions, while "app" will.
	""")
	private Integer prefixLength;

	@Nullable
	@Description("""
		The minimum length of a word for it to be considered for suggestions.
		Terms shorter than this length will not generate suggestions.
	""")
	private Integer minWordLength;

	@Nullable
	@Description("""
		The maximum allowed edit distance for suggestions. Valid values are in the range [1, 2].
		The default value is 2. Higher edit distances can lead to more suggestions, but also to less relevant ones.
	""")
	private Integer maxEdit;

	@Nullable
	@Description("""
		If enabled, the search query will automatically be replaced with the corrected version from the autocorrection suggester.
	""")
	private Boolean enableSearchWithCorrection;
}
