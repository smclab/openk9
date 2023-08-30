package io.openk9.datasource.model.dto;

import io.openk9.datasource.model.dto.util.K9EntityDTO;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TranslationDTO extends K9EntityDTO {
	private String language;
	private String key;
	private String value;
}
