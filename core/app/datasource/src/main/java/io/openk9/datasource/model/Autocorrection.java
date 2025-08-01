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

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.openk9.datasource.model.util.K9Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

@Entity
@Table(name = "autocorrection")
@Getter
@Setter
@ToString
public class Autocorrection extends K9Entity {

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "description", length = 4096)
	private String description;

	@JsonIgnore
	@ToString.Exclude
	@ManyToOne
	@JoinColumn(name = "autocorrection_doc_type_field_id", referencedColumnName = "id")
	private DocTypeField autocorrectionDocTypeField;

	@Enumerated(EnumType.STRING)
	@Column(name = "sort", nullable = false)
	private SortType sort = SortType.SCORE;

	@Enumerated(EnumType.STRING)
	@Column(name = "suggest_mode", nullable = false)
	private SuggestMode suggestMode = SuggestMode.MISSING;

	@Column(name = "prefix_length")
	private Integer prefixLength;

	@Column(name = "min_word_length")
	private Integer minWordLength;

	@Column(name = "max_edit")
	private Integer maxEdit;

	@Column(name = "enable_search_with_correction")
	private boolean enableSearchWithCorrection = false;

	public void setSort(SortType sort) {
		this.sort = Objects.requireNonNullElse(sort, SortType.SCORE);
	}

	public void setSuggestMode(SuggestMode suggestMode) {
		this.suggestMode = Objects.requireNonNullElse(suggestMode, SuggestMode.MISSING);
	}
}
