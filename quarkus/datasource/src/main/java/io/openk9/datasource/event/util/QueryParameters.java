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

package io.openk9.datasource.event.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Builder
public class QueryParameters {

	private Collection<String> fields;
	private Collection<String> projections;
	private List<? extends Sortable> orderBy;
	private SortType sortType;
	private Integer limit;
	private Integer offset;
	private Boolean distinct;

	public static final String FIELDS = "fields";
	public static final String PROJECTIONS = "projections";
	public static final String ORDER_BY = "orderBy";
	public static final String LIMIT = "limit";
	public static final String OFFSET = "offset";
	public static final String DISTINCT = "distinct";
	public static final String SORT_TYPE = "sortType";

}
