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
import com.google.protobuf.Value;
import io.openk9.datasource.grpc.BehaviorMergeType;
import io.openk9.datasource.grpc.BehaviorOnError;
import io.openk9.datasource.grpc.CreateEnrichItemRequest;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnrichItemMapperTest {

	@Test
	void map() {
		var enrichItemDTO = EnrichItemMapper.INSTANCE.map(CreateEnrichItemRequest.newBuilder()
			.setSchemaName("mew")
			.setName("item1")
			.setDescription("desc1")
			.setBehaviorMergeType(BehaviorMergeType.MERGE)
			.setBehaviorOnErrorType(BehaviorOnError.FAIL)
			.setJsonPath("$.root")
			.setJsonConfig(Struct.newBuilder()
				.putFields("k1", Value.newBuilder()
					.setStringValue("v1")
					.build()
				)
				.putFields("k2", Value.newBuilder()
					.setStructValue(Struct.newBuilder()
						.putFields("k3", Value.newBuilder()
							.setBoolValue(false)
							.build())
						.build())
					.build())
				.build()
			)
			.build());

		var jsonConfig = enrichItemDTO.getJsonConfig();
		var jsonObject = new JsonObject(jsonConfig);

		assertEquals("v1", jsonObject.getString("k1"));
		assertEquals(false, jsonObject.getJsonObject("k2").getBoolean("k3"));
	}

}