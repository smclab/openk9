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

package io.openk9.datasource.resource.util;

import io.openk9.datasource.graphql.util.SortType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Pageable {
	@QueryParam("limit") @DefaultValue("20")
	private int limit;
	@QueryParam("offset") @DefaultValue("0")
	private int offset;
	@QueryParam("sortBy") @DefaultValue("createDate")
	private K9Column sortBy;
	@QueryParam("sortType") @DefaultValue("ASC")
	private SortType sortType;
}
