package io.openk9.datasource.graphql.dto;

import io.openk9.datasource.model.UserField;
import io.openk9.datasource.model.dto.PluginDriverDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PluginWithDocTypeDTO extends PluginDriverDTO {

	private Set<DocTypeUserDTO> docTypeUserDTOSet;

	@SuperBuilder
	@Getter
	@Setter
	@ToString
	@NoArgsConstructor
	public static class DocTypeUserDTO {
		private long docTypeId;
		private UserField userField;
	}

}
