package io.openk9.datasource.web.dto;

import io.openk9.datasource.model.DocTypeField;

public record PartialDocTypeFieldDTO(String field, Long id, String label) {

	public static PartialDocTypeFieldDTO of(DocTypeField docTypeField) {
		return new PartialDocTypeFieldDTO(
			docTypeField.getPath(),
			docTypeField.getId(),
			docTypeField.getName()
		);
	}

}
