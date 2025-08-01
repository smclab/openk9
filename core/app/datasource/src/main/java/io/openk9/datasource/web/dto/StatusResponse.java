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

package io.openk9.datasource.web.dto;

import java.util.List;

import io.openk9.datasource.service.SchedulerService;

import lombok.Getter;

@Getter
public class StatusResponse {

	private final List<SchedulerService.DatasourceHealthStatus> datasources;
	private final int total;
	private int errors;

	public StatusResponse(List<SchedulerService.DatasourceHealthStatus> datasources) {
		this.datasources = datasources;
		this.total = datasources.size();
		this.errors = 0;

		for (SchedulerService.DatasourceHealthStatus datasource : datasources) {
			assert datasource.status() != null;

			if (datasource.status() == SchedulerService.HealthStatus.ERROR) {
				errors++;
			}

		}

	}

}
