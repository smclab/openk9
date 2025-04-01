package io.openk9.datasource.model.dto;

import io.openk9.datasource.model.RAGType;
import io.openk9.datasource.model.dto.util.K9EntityDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class RAGConfigurationDTO extends K9EntityDTO {

	private Integer chunkWindow;
	private String prompt;
	private String promptNoRag;
	private String ragToolDescription;
	private Boolean reformulate;
	private String rephrasePrompt;
	@NotNull
	private RAGType type;
}
