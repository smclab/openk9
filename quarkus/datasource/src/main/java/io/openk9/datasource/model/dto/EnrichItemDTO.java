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

import io.openk9.datasource.graphql.util.JsonObjectAdapter;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.dto.util.K9EntityDTO;
import io.openk9.datasource.validation.JavascriptScript;
import io.smallrye.graphql.api.AdaptWith;
import io.vertx.core.json.JsonObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class EnrichItemDTO extends K9EntityDTO {
	@NotNull
	private EnrichItem.EnrichItemType type;
	@NotNull
	@NotEmpty
	private String serviceName;
	@JavascriptScript
	private String validationScript;
	@AdaptWith(JsonObjectAdapter.class)
	private JsonObject jsonConfig;
}
