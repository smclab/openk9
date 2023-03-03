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

package io.openk9.datasource.model.dto;

import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.dto.util.K9EntityDTO;
import io.openk9.datasource.validation.json.Json;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.graphql.Description;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class DocTypeFieldDTO extends K9EntityDTO {

	@NotNull
	@Description("If true field is used for matches during search")
	private Boolean searchable = false;

	@NotNull
	@Description("If true field is used for sorting during search")
	private Boolean sorteable = false;

	@Description("Value to define boost on score in case of matches on current field")
	private Double boost = 1.0;

	@Description("Define type used to map field in index")
	@NotNull
	private FieldType fieldType;

	@Description("If true field is not returned by search")
	private Boolean exclude;

	@NotNull
	private String fieldName;

	@Json
	private String jsonConfig;
}
