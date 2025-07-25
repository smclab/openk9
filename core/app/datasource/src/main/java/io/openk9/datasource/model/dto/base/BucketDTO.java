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

package io.openk9.datasource.model.dto.base;

import jakarta.validation.constraints.NotNull;

import io.openk9.datasource.model.Bucket;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class BucketDTO extends K9EntityDTO {

	@NotNull
	@Builder.Default
	private Boolean refreshOnSuggestionCategory = false;

	@NotNull
	@Builder.Default
	private Boolean refreshOnTab = false;

	@NotNull
	@Builder.Default
	private Boolean refreshOnDate = false;

	@NotNull
	@Builder.Default
	private Boolean refreshOnQuery = false;

	@NotNull
	@Builder.Default
	private Bucket.RetrieveType retrieveType = Bucket.RetrieveType.TEXT;

}
