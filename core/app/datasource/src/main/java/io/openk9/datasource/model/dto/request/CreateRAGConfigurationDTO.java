package io.openk9.datasource.model.dto.request;

import io.openk9.datasource.model.RAGType;
import io.openk9.datasource.model.dto.base.RAGConfigurationDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
@NoArgsConstructor
public class CreateRAGConfigurationDTO extends RAGConfigurationDTO {
	@NotNull
	private RAGType type;
}
