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

package io.openk9.ingestion.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@RegisterForReflection
@ToString
public class IngestionDTO {
	private long datasourceId;
	private String contentId;
	private long parsingDate;
	private String rawContent;
	private Map<String, Object> datasourcePayload;
	private ResourcesDTO resources;
	private Map<String, List<String>> acl;
	private String scheduleId;
}