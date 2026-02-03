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

import io.smallrye.graphql.api.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.graphql.Description;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class HighlightDTO extends K9EntityDTO{

	@Description("""
		The field used to chose the highlighter.
		""")
	private String type;

	@Description("""
		Set of unique DocTypeField IDs, it's used to add the fields to highlight.
		""")
	private Set<Long> fieldIds;

	@Description("""
		Specifies how to split fragments of text, for unified and fvh highlighters.
		""")
	private String boundaryScanner;

	@Nullable
	@Description("""
		The array of chars allowed to split fragments of text, only for fvh highlighter.
		""")
	private char[] boundaryChars;

	@Description("""
		Specifies how to split fragments of text, only for plain highlighter.
		""")
	private String fragmenter;

	@Description("""
		Maximum length of each fragment.
		""")
	private int fragmentSize;

	@Description("""
		Maximum number of fragments to return.
		""")
	private int numberOfFragments;

	@Description("""
		Field that decides how to order fragments of text.
		""")
	private String order;

	@Nullable
	@Description("""
		Set of unique DocTypeField IDs, it's used to highlighting fields indexed with different analyzers.
		""")
	private Set<Long> matchedFieldIds;

}
