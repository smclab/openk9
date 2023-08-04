package io.openk9.datasource.web.dto;

import java.util.List;
import java.util.Map;

public record TabResponseDTO(
	String label, List<TokenTabResponseDTO> tokens, Map<String, String> translationMap) {
}
