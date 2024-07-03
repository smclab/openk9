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

package io.openk9.datasource.pipeline.actor;

import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import io.openk9.common.util.ShardingKey;
import io.openk9.datasource.pipeline.base.BasePipeline;
import io.openk9.datasource.pipeline.vector.VectorPipeline;

public class SchedulingEntityType {

	public static EntityTypeKey<Scheduling.Command> getTypeKey(ShardingKey shardingKey) {

		var scheduleId = shardingKey.scheduleId();

		if (scheduleId.endsWith(VectorPipeline.VECTOR_PIPELINE_SUFFIX)) {
			return VectorPipeline.ENTITY_TYPE_KEY;
		}

		return BasePipeline.ENTITY_TYPE_KEY;

	}

}
