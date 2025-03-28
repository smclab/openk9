package io.openk9.datasource.model.dto;

import io.openk9.datasource.model.RAGType;
import io.openk9.datasource.model.dto.util.K9EntityDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class RAGConfigurationDTO extends K9EntityDTO {

	private RAGType type;
	private String prompt;
	private String rephrasePrompt;
	private String promptNoRag;
	private String ragToolDescription;
	private int chunkWindow;
	private boolean reformulate;
}
