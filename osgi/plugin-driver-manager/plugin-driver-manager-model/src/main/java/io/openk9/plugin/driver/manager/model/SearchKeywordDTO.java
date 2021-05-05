package io.openk9.plugin.driver.manager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Builder
public class SearchKeywordDTO {
	private String keyword;
	private boolean text;
	private FieldBoostDTO fieldBoost;
}
