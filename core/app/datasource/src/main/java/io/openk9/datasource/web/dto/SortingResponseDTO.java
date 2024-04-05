package io.openk9.datasource.web.dto;

import java.util.List;
import java.util.Map;

public record SortingResponseDTO(
	Long id, String label, String type, String field, boolean isDefault, Map<String, String> translationMap) {
}
