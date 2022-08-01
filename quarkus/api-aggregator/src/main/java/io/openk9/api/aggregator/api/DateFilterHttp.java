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

package io.openk9.api.aggregator.api;

import io.openk9.api.aggregator.client.dto.DateFilterDTO;
import io.smallrye.mutiny.Uni;

import javax.annotation.security.PermitAll;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.util.List;

public interface DateFilterHttp {

	@PermitAll
	@GET
	@Path("/v2/date-filter/count")
	public Uni<Long> dateFilterCount();

	@PermitAll
	@GET
	@Path("/v2/date-filter/{id}")
	public Uni<DateFilterDTO> dateFilterFindById(@PathParam("id") long id);

	@Path("/v2/date-filter")
	@PermitAll
	@GET
	public Uni<List<DateFilterDTO>> dateFilterFindAll(
		@QueryParam("sort") List<String> dateFilterSortQuery,
		@QueryParam("page") @DefaultValue("0") int pageIndex,
		@QueryParam("size") @DefaultValue("20") int pageSize
	);

}
