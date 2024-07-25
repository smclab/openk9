package io.openk9.datasource.graphql.dto;

import io.openk9.datasource.model.dto.SuggestionCategoryDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SuggestionCategoryWithDocTypeFieldDTO extends SuggestionCategoryDTO {
	private Set<Long> docTypeFieldIds;
}
