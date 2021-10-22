package io.openk9.datasource.client.plugindriver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor(staticName = "of")
@Builder
public class SearchKeywordDTO {
	private String keyword;
	private boolean text;
	private FieldBoostDTO fieldBoost;
}
