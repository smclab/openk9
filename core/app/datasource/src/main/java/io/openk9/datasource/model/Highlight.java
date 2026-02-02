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

package io.openk9.datasource.model;

import io.openk9.datasource.model.util.K9Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity(name = Highlight.ENTITY_NAME)
@Table(name = Highlight.TABLE_NAME)
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Highlight extends K9Entity {

	public static final String ENTITY_NAME = "Highlight";
	public static final String TABLE_NAME = "highlight";

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "description", length = 4096)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private HighlightType type;

	@ToString.Exclude
	@Column(name = "fields")
	private Set<DocTypeField> fields = new LinkedHashSet<>();

	@Enumerated(EnumType.STRING)
	@Column(name = "boundary_scanner")
	private BoundaryScannerType boundaryScanner;

	@Column(name = "boundary_chars")
	private char[] boundaryChars;

	@Enumerated(EnumType.STRING)
	@Column(name = "fragmenter")
	private FragmenterType fragmenter;

	@Column(name = "fragment_size")
	private int fragmentSize;

	@Column(name = "number_of_fragments")
	private int numberOfFragments;

	@Enumerated(EnumType.STRING)
	@Column(name = "order")
	private OrderType order;

	@ToString.Exclude
	@Column(name = "matched_fields")
	private Set<DocTypeField> matchedFields = new LinkedHashSet<>();


	public enum HighlightType {
		UNIFIED, FVH, PLAIN
	}

	public enum BoundaryScannerType {
		SENTENCE, WORD, CHARS
	}

	public enum FragmenterType {
		SPAM, SIMPLE
	}

	public enum OrderType {
		NONE, SCORE
	}
}
