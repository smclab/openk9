package io.openk9.plugin.driver.manager.api.config;

import io.openk9.plugin.driver.manager.api.SearchKeyword;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchKeywordConfig {
	private SearchKeyword.Type type;
	private String keyword;
	private Map<String, Object> options;
}
