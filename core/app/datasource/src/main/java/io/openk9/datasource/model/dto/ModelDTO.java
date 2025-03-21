package io.openk9.datasource.model.dto;

import groovy.transform.EqualsAndHashCode;
import io.openk9.datasource.model.dto.util.K9EntityDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.graphql.Description;

@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ModelDTO extends K9EntityDTO {
	@Description("It is the model type.")
	private String type;
	@Description("It is the specific model.")
	private String model;
}
