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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import io.openk9.datasource.model.util.K9Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "query_parser_config")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class QueryParserConfig extends K9Entity {

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "type", nullable = false)
	@Enumerated(EnumType.STRING)
	private QueryParserType type;

	@Column(name = "description", length = 4096)
	private String description;

	@JdbcTypeCode(SqlTypes.LONG32VARCHAR)
	@Column(name = "json_config")
	private String jsonConfig;

	@ToString.Exclude
	@ManyToOne(cascade = jakarta.persistence.CascadeType.ALL)
	@JoinColumn(name = "search_config")
	@JsonIgnore
	private SearchConfig searchConfig;
}