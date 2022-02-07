package io.openk9.plugin.driver.manager.api.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginDriverConfig {
	private String name;
	private String driverServiceName;
	private boolean schedulerEnabled;
	private Type type = Type.HTTP;
	private Map<String, Object> options;
	private List<DocumentTypeConfig> documentTypes;
	private List<EnrichProcessorConfig> enrichProcessors;
	public enum Type {
		HTTP
	}
}
