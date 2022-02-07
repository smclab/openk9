package io.openk9.plugin.driver.manager.api.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTypeConfig {
	private String name;
	private String icon;
	private boolean defaultDocumentType;
	private List<SearchKeywordConfig> searchKeywords;
	private Map<String, Object> mappings;
}
