package io.openk9.datasource.web.dto;

import java.util.List;
import java.util.Map;

public record SortingResponseDTO(
	String label, String type, Map<String, String> translationMap) {
}
