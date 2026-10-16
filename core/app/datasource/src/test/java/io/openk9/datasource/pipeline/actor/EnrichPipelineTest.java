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

import java.util.HashMap;
import java.util.Map;

import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.processor.payload.DataPayload;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EnrichPipelineTest {

	private static DataPayload payload(String rawContent, Map<String, Object> rest) {
		return DataPayload.builder()
			.rawContent(rawContent)
			.rest(new HashMap<>(rest))
			.build();
	}

	@Test
	void mergeReplacesRawContentWithAnEmptyOne() {
		var prev = payload("connector text", Map.of());
		var next = payload("", Map.of());

		var merged = EnrichPipeline.mergeResponse(
			"$", EnrichItem.BehaviorMergeType.REPLACE, prev, next);

		Assertions.assertEquals("", merged.getRawContent());
	}

	@Test
	void mergeReplacesRawContentAtTheTopLevelWithANestedJsonPath() {
		var prev = payload("connector text", Map.of("title", "report"));
		var next = payload("extracted text", Map.of("content", "extracted text"));

		var merged = EnrichPipeline.mergeResponse(
			"$.tika", EnrichItem.BehaviorMergeType.REPLACE, prev, next);

		// the jsonPath moves only the rest under tika, not the rawContent
		Assertions.assertEquals("extracted text", merged.getRawContent());
		Assertions.assertEquals("report", merged.getRest().get("title"));
		Assertions.assertFalse(merged.getRest().containsKey("content"));
		Assertions.assertEquals(
			"extracted text",
			new JsonObject(merged.getRest()).getJsonObject("tika").getString("content"));
	}

	@Test
	void mergeReplacesRawContentAlsoWithTheMergeBehavior() {
		var prev = payload("connector text", Map.of());
		var next = payload("extracted text", Map.of());

		var merged = EnrichPipeline.mergeResponse(
			"$", EnrichItem.BehaviorMergeType.MERGE, prev, next);

		Assertions.assertEquals("extracted text", merged.getRawContent());
	}

}
