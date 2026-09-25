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

package io.openk9.datasource.service;

import java.util.List;

import io.openk9.datasource.model.dto.request.HybridSearchPipelineDTO;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SearchConfigPipelineBodyTest {

	@Test
	void getJsonBody_emptyDto_usesTheDefaults() {
		var body = processor(HybridSearchPipelineDTO.builder()
			.normalizationTechnique(null)
			.combinationTechnique(null)
			.weights(null)
			.build());

		Assertions.assertEquals(
			"min_max",
			body.getJsonObject("normalization").getString("technique"));
		Assertions.assertEquals(
			"arithmetic_mean",
			body.getJsonObject("combination").getString("technique"));
		Assertions.assertEquals(
			"[0.3,0.7]",
			body.getJsonObject("combination")
				.getJsonObject("parameters")
				.getJsonArray("weights")
				.toString());
	}

	@Test
	void getJsonBody_onlyWeights_keepsTheDefaultTechniques() {
		var body = processor(HybridSearchPipelineDTO.builder()
			.normalizationTechnique(null)
			.combinationTechnique(null)
			.weights(List.of(0.5d, 0.5d))
			.build());

		Assertions.assertEquals(
			"min_max",
			body.getJsonObject("normalization").getString("technique"));
		Assertions.assertEquals(
			"[0.5,0.5]",
			body.getJsonObject("combination")
				.getJsonObject("parameters")
				.getJsonArray("weights")
				.toString());
	}

	private static jakarta.json.JsonObject processor(
		HybridSearchPipelineDTO pipelineDTO) {

		return SearchConfigService.getJsonBody(pipelineDTO)
			.getJsonArray("phase_results_processors")
			.getJsonObject(0)
			.getJsonObject("normalization-processor");
	}

}
