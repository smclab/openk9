package io.openk9.datasource.client.plugindriver.dto;

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
	private String driverServiceName;
	private String name;
	private boolean schedulerEnabled;
	private List<DocumentTypeDTO> documentTypes;
	private DocumentTypeDTO defaultDocumentType;
}
