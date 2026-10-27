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

import io.openk9.datasource.validation.ValidQuartzCron;
import io.openk9.datasource.validation.json.Json;
import io.openk9.ml.grpc.EmbeddingOuterClass;

import io.smallrye.graphql.api.Nullable;
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
	@Nullable
	@Description("""
		Define if the first dataIndex is a knn index, this property enables
		vector similarity search features on it. It and the other properties
		that shape the first dataIndex are read only when the datasource has
		no dataIndex to inherit from. When omitted on update, the current
		value is kept.""")
	private Boolean knnIndex;
	@Nullable
	@Description("The number of chunks before and after every chunk.")
	private Integer chunkWindowSize;
	@Nullable
	@Description("The chunk strategy to apply.")
	private EmbeddingOuterClass.ChunkType chunkType;
	@Json
	@Nullable
	@Description("The configurations used by the embedding model, if needed.")
	private String embeddingJsonConfig;
	@Json
	@Nullable
	@Description("The settings that will be used when the associated indexTemplate is created.")
	private String settings;

}
