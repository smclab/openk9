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

package io.openk9.datasource.model;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
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

import io.openk9.datasource.model.util.Fuzziness;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.validation.ValidAutocompleteFields;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "autocomplete")
@Getter
@Setter
@ToString
public class Autocomplete extends K9Entity {

	public static final Fuzziness DEFAULT_FUZZINESS = Fuzziness.AUTO;
	public static final String DEFAULT_MINIMUM_SHOULD_MATCH = "75%";
	public static final BooleanOperator DEFAULT_OPERATOR = BooleanOperator.OR;
	public static final int RESULT_SIZE = 10;
	@Column(name = "description", length = 4096)
	private String description;
	@ManyToMany(cascade = {
		jakarta.persistence.CascadeType.PERSIST,
		jakarta.persistence.CascadeType.MERGE,
		jakarta.persistence.CascadeType.DETACH,
		jakarta.persistence.CascadeType.REFRESH
	}
	)
	@JoinTable(name = "autocomplete_doc_type_field",
		joinColumns = @JoinColumn(name = "autocomplete_id", referencedColumnName = "id"),
		inverseJoinColumns = @JoinColumn(name = "doc_type_field_id", referencedColumnName = "id"))
	@ToString.Exclude
	@JsonIgnore
	@ValidAutocompleteFields
	@NotNull
	@NotEmpty
	private Set<DocTypeField> fields = new LinkedHashSet<>();
	@Enumerated(EnumType.STRING)
	@Column(name = "fuzziness")
	private Fuzziness fuzziness = Fuzziness.AUTO;
	@Column(name = "minimum_should_match")
	private String minimumShouldMatch = DEFAULT_MINIMUM_SHOULD_MATCH;
	@Column(name = "name", nullable = false, unique = true)
	private String name;
	@Enumerated(EnumType.STRING)
	@Column(name = "operator")
	private BooleanOperator operator = DEFAULT_OPERATOR;
	@Column(name = "perfect_match_included")
	private boolean perfectMatchIncluded = false;
	@Column(name = "result_size")
	private Integer resultSize = RESULT_SIZE;

	public void setFuzziness(Fuzziness fuzziness) {
		this.fuzziness = Objects.requireNonNullElse(fuzziness, Fuzziness.AUTO);
	}

	public void setMinimumShouldMatch(String minimumShouldMatch) {
		this.minimumShouldMatch =
			Objects.requireNonNullElse(minimumShouldMatch, DEFAULT_MINIMUM_SHOULD_MATCH);
	}

	public void setOperator(BooleanOperator operator) {
		this.operator = Objects.requireNonNullElse(operator, DEFAULT_OPERATOR);
	}

	public void setResultSize(Integer resultSize) {
		this.resultSize =
			Objects.requireNonNullElse(resultSize, RESULT_SIZE);
	}
}
