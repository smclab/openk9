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

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Builder
public class HybridSearchPipelineDTO {

	public static final HybridSearchPipelineDTO DEFAULT =
		new HybridSearchPipelineDTOBuilder().build();

	@Builder.Default
	private NormalizationTechnique normalizationTechnique = NormalizationTechnique.MIN_MAX;
	@Builder.Default
	private CombinationTechnique combinationTechnique = CombinationTechnique.ARITHMETIC_MEAN;
	@Builder.Default
	private List<Double> weights = List.of(0.3d, 0.7d);

	@Getter
	public enum NormalizationTechnique {

		MIN_MAX("min_max"),
		L2("l2");

		private final String value;

		NormalizationTechnique(String value) {
			this.value = value;
		}

	}

	@Getter
	public enum CombinationTechnique {

		ARITHMETIC_MEAN("arithmetic_mean"),
		GEOMETRIC_MEAN("geometric_mean"),
		ARMONIC_MEAN("armonic_mean");

		private final String value;

		CombinationTechnique(String value) {
			this.value = value;
		}

	}

}
