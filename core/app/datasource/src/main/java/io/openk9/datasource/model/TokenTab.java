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
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Entity
@Table(name = "token_tab")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class TokenTab extends K9Entity {

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "description", length = 4096)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "token_type", nullable = false)
	private TokenType tokenType;

	@Column(name = "value")
	private String value;

	@Column(name ="filter", nullable = false)
	private Boolean filter;

	@OneToOne(
		fetch = FetchType.LAZY
	)
	@JoinColumn(name = "doc_type_field_id")
	@JsonIgnore
	@ToString.Exclude
	private DocTypeField docTypeField;

	@ElementCollection
	@CollectionTable(
		name = "token_tab_extra_params",
		joinColumns = @JoinColumn(name = "token_tab_id")

	)
	@MapKeyColumn(name = "key")
	@Column(name = "value")
	@JsonIgnore
	private Map<String, String> extraParams = new HashMap<>();

	public enum TokenType {
		DATE, DOCTYPE, TEXT, ENTITY, AUTOCOMPLETE, FILTER, DATE_ORDER
	}

	public void addExtraParam(String key, String value) {
		extraParams.put(key, value);
	}

	public void removeExtraParam(String key) {
		extraParams.remove(key);
	}

	public static Set<ExtraParam> getExtraParamsSet(Map<String, String> extraParams) {
		return extraParams
			.entrySet()
			.stream().map(e -> new ExtraParam(e.getKey(), e.getValue()))
			.collect(Collectors.toSet());
	}

	public record ExtraParam(String key, String value) {}
}
