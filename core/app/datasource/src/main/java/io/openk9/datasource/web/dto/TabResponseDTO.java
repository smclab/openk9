package io.openk9.datasource.web.dto;

import java.util.List;

public record TabResponseDTO(
	String label, List<TokenTabResponseDTO> tokens) {
}
