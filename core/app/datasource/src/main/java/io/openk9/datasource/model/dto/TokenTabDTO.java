package io.openk9.datasource.model.dto;


import io.openk9.datasource.model.TokenTab;
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
public class TokenTabDTO extends K9EntityDTO {

	@NotNull
	private TokenTab.TokenType tokenType;

	@NotNull
	@NotEmpty
	private String value;

	@NotNull
	private Boolean filter;

}
