package io.openk9.datasource.web;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
public class SearchTokenDto {
	private String tokenType;
	private String keywordKey;
	private List<String> values;
	private Boolean filter;
}
