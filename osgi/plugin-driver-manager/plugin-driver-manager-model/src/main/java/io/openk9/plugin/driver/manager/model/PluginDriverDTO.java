package io.openk9.plugin.driver.manager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Builder
public class PluginDriverDTO {
	private String name;
	private boolean schedulerEnabled;
	private List<DocumentTypeDTO> documentTypes;
	private DocumentTypeDTO defaultDocumentType;
}
