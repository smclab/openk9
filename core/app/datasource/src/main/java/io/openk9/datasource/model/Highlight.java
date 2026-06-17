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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.openk9.datasource.model.util.K9Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "highlight")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Highlight extends K9Entity {

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "description", length = 4096)
	private String description;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private HighlightType type;

	@JoinTable(
		name = "highlight_fields",
		joinColumns = @JoinColumn(name = "highlight_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "doc_type_field_id", referencedColumnName = "id")
	)
	@ManyToMany
	@JsonIgnore
	@ToString.Exclude
	@NotNull
	@NotEmpty
	private Set<DocTypeField> fields = new LinkedHashSet<>();

	@Enumerated(EnumType.STRING)
	@Column(name = "boundary_scanner")
	private BoundaryScannerType boundaryScanner;

	@Column(name = "boundary_chars")
	private String boundaryChars;

	@Enumerated(EnumType.STRING)
	@Column(name = "fragmenter")
	private FragmenterType fragmenter;

	@Column(name = "fragment_size")
	private int fragmentSize;

	@Column(name = "number_of_fragments")
	private int numberOfFragments;

	@Enumerated(EnumType.STRING)
	@Column(name = "fragments_order")
	private OrderType order;

	@JoinTable(
		name = "highlight_matched_fields",
		joinColumns = @JoinColumn(name = "highlight_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "doc_type_field_id", referencedColumnName = "id")
	)
	@ManyToMany
	@JsonIgnore
	@ToString.Exclude
	private Set<DocTypeField> matchedFields = new LinkedHashSet<>();


	public enum HighlightType {
		UNIFIED, FVH, PLAIN
	}

	public enum BoundaryScannerType {
		SENTENCE, WORD, CHARS
	}

	public enum FragmenterType {
		SPAN, SIMPLE
	}

	public enum OrderType {
		NONE, SCORE
	}
}
