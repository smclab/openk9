package io.openk9.datasource.model.dto;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.eclipse.microprofile.graphql.Description;

@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@Embeddable
public class ModelTypeDTO {
	@Description("It is the model type.")
	private String type;
	@Description("It is the specific model.")
	private String model;
}
