package io.openk9.datasource.model.dto;

import io.openk9.datasource.model.dto.util.K9EntityDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class FileResourceDTO extends K9EntityDTO {

	@NotNull
	@NotEmpty
	private String datasourceId;

	@NotNull
	@NotEmpty
	private String fileId;

	@NotNull
	@NotEmpty
	private String resourceId;
}
