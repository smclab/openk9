package io.openk9.datasource.web.dto;

import io.openk9.datasource.model.DocTypeField;

import java.util.Map;

public record DocTypeFieldResponseDTO(String field, Long id, String label, Map<String, String> translationMap) {

}
