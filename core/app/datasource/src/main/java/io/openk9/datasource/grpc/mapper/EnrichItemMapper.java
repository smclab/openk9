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

package io.openk9.datasource.grpc.mapper;

import com.google.protobuf.Struct;
import io.openk9.datasource.grpc.BehaviorMergeType;
import io.openk9.datasource.grpc.BehaviorOnError;
import io.openk9.datasource.grpc.CreateEnrichItemRequest;
import io.openk9.datasource.grpc.EnrichItemType;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.dto.EnrichItemDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ValueMapping;
import org.mapstruct.ValueMappings;

@Mapper(componentModel = "cdi")
public interface EnrichItemMapper {

	EnrichItemDTO map(CreateEnrichItemRequest source);

	@ValueMappings(
		{
			@ValueMapping(source = "HTTP_ASYNC", target = "HTTP_ASYNC"),
			@ValueMapping(source = "HTTP_SYNC", target = "HTTP_SYNC"),
			@ValueMapping(source = "GROOVY_SCRIPT", target = "GROOVY_SCRIPT")
		}
	)
	EnrichItem.EnrichItemType map(EnrichItemType source);

	@ValueMappings(
		{
			@ValueMapping(source = "MERGE", target = "MERGE"),
			@ValueMapping(source = "REPLACE", target = "REPLACE")
		}
	)
	EnrichItem.BehaviorMergeType map(BehaviorMergeType source);

	@ValueMappings(
		{
			@ValueMapping(source = "SKIP", target = "SKIP"),
			@ValueMapping(source = "FAIL", target = "FAIL"),
			@ValueMapping(source = "REJECT", target = "REJECT")
		}
	)
	EnrichItem.BehaviorOnError map(BehaviorOnError source);

	default String map(Struct value) {
		return value.toString();
	}

}
