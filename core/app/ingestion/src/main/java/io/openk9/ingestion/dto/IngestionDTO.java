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

import io.openk9.common.util.ingestion.PayloadType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@RegisterForReflection
public class IngestionDTO {
	@Schema(format = "uuid",
			description = "Unique id that identifies the datasource the ingested message belongs to.")
	private long datasourceId;
	@Schema(format = "uuid",
			description = "Unique string that identifies the tenant the ingested message belongs to.")
	private String tenantId;
	@Schema(format = "uuid",
			description = "Unique id by datasource that identifies the resource inside Openk9.")
	private String contentId;
	@Schema(format = "date",
			description = "Date when scheduling associated with message is started.")
	private long parsingDate;
	@Schema(description = "Message raw content. Can be used to perform some elaboration or enrichment inside Openk9 pipeline.")
	private String rawContent;
	private Map<String, Object> datasourcePayload;
	@Schema(description = "Object to pass resources associated with message.")
	private ResourcesDTO resources;
	@Schema(description = "Object to pass access control list associated with message.")
	private Map<String, List<String>> acl;
	@Schema(format = "uuid",
			description = "Unique string that identifies the scheduling the ingested message belongs to.")
	private String scheduleId;
	@Schema(description = "Specify if it is the last message of scheduling.")
	private boolean last = false;
	@Schema(description = "String used to specify type associated with message. Default is DOCUMENT for message. HALT is used when a message to stop scheduling arrived.")
	private PayloadType type;
}