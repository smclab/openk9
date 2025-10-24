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

package io.openk9.datasource.pipeline.service.dto;

import java.time.OffsetDateTime;
import java.util.Set;

import io.openk9.datasource.index.model.IndexName;
import io.openk9.datasource.model.Scheduler;

import lombok.Data;

@Data
public class SchedulerDTO {
	private Long id;
	private String tenantId;
	private String scheduleId;
	private Long datasourceId;
	private Set<EnrichItemDTO> enrichItems;
	private Long oldDataIndexId;
	private String oldDataIndexName;
	private Long newDataIndexId;
	private String newDataIndexName;
	private Scheduler.SchedulerStatus status;
	private OffsetDateTime lastIngestionDate;
	private SchedulingType schedulingType;
	private boolean reindex;

	public String getIndexName() {

		String newIndexName = getNewIndexName();

		return newIndexName != null ? newIndexName : getOldIndexName();
	}

	public String getNewIndexName() {
		if (tenantId == null || newDataIndexName == null) {
			return null;
		}

		return IndexName.from(tenantId, newDataIndexName).toString();
	}

	public String getOldIndexName() {
		if (tenantId == null || oldDataIndexName == null) {
			return null;
		}

		return IndexName.from(tenantId, oldDataIndexName).toString();
	}

}
