package io.openk9.datasource.web.dto;

import java.util.List;
import java.util.Map;

public record TokenTabResponseDTO(
	String tokenType,
	String keywordKey,
	boolean filter,
	List<String> values,
	Map<String, String> extra) {
}
