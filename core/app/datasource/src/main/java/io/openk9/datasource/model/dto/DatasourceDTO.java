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

import io.openk9.datasource.model.PipelineType;
import io.openk9.datasource.model.dto.util.K9EntityDTO;
import io.openk9.datasource.validation.ValidQuartzCron;
import io.openk9.datasource.validation.json.Json;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.graphql.Description;

@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class DatasourceDTO extends K9EntityDTO {

	@Json
	@Description("Json configuration with custom fields for datasource")
	private String jsonConfig;
	@Description("If true set active the purge job scheduling")
	private Boolean purgeable;
	@ValidQuartzCron
	@Description("Cron quartz expression to define purging for this datasource")
	private String purging;
	@Description("The duration to identify orphaned Dataindex.")
	private String purgeMaxAge;
	@Description("If true datasource is reindexed based on defined scheduling expression")
	private Boolean reindexable;
	@ValidQuartzCron
	@Description("Cron quartz expression to define reindexing of datasource")
	private String reindexing;
	@Description("If true datasource is scheduled based on defined scheduling expression")
	private Boolean schedulable;
	@ValidQuartzCron
	@Description("Cron quartz expression to define scheduling of datasource")
	private String scheduling;
	@Description("""
		Defines what kind of pipeline you want to run during the ingestion.
		Possible kind are:
			- Enrich: running the enrichPipeline defined in the Datasource;
			- Embedding: create an embedding vector using the active EmbeddingModel
			in the tenant. You have to define the field from what you want to
			embed information.
			- Enrich and Embedding: you want to run first the enrichPipeline and
			then you want to create an embedding from a field.
		""")
	private PipelineType pipelineType;

}
