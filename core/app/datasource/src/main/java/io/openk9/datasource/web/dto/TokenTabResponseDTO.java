package io.openk9.datasource.web.dto;

import java.util.List;

public record TokenTabResponseDTO(
	String tokenType, String keywordKey, boolean filter, List<String> values) {
}
