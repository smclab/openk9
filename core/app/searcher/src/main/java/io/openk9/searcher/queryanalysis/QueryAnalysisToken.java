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

package io.openk9.searcher.queryanalysis;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class QueryAnalysisToken {
	@Schema(description = "Text associated to analysis")
	private String text;
	@Schema(description = "Character offset where analysis starts")
	private Integer start;
	@Schema(description = "Character offset where analysis ends")
	private Integer end;
	@Schema(description = "List of token associated to analysis")
	private Map<String, Object> token;
	@Schema(description = "Token position for analysis")
	private Integer[] pos;
}