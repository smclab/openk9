package io.openk9.plugin.driver.manager.api.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrichProcessorConfig {
	private String name;
	private Type type;
	private Map<String, Object> options;
	public enum Type {
		ASYNC, SYNC
	}
}
