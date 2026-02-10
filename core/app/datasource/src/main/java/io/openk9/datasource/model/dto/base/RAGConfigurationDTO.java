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

import io.openk9.datasource.validation.json.Json;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.graphql.Description;

@Data
@SuperBuilder
@NoArgsConstructor
public class RAGConfigurationDTO extends K9EntityDTO {

	@Description(
		"""
			Controls context window merging behavior for chunk processing:
			0: Disables chunk merging.
			> 0: Enables merging with specified window size.
			"""
	)
	private Integer chunkWindow;
	@Description("Boolean flag to show conversations title.")
	private Boolean enableConversationTitle;
	@Json
	@Description("A JSON that can be used to add additional configurations to the EmbeddingModel.")
	private String jsonConfig;
	@Description("Main prompt template used for RAG.")
	private String prompt;
	@Description(
		"""
			Prompt template used specifically in RAG-as-tool configurations when the RAG
			tool is available but not invoked by the LLM.
			"""
	)
	private String promptNoRag;
	@Description(
		"""
			Description of the RAG tool's capabilities, used in RAG-as-tool implementations
			to help the LLM decide when to invoke it.
			"""
	)
	private String ragToolDescription;
	@Description(
		"""
			Boolean flag that controls whether a large language model should reformulate
			the input prompt before processing it using rephrasePrompt.
			"""
	)
	private Boolean reformulate;
	@Description("Prompt template used if reformulate is set to true.")
	private String rephrasePrompt;
}
