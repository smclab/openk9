package io.openk9.datasource.events;

public record DatasourceEvent(
		String ingestionId,
		long datasourceId,
		String contentId,
		long parsingDate,
		String rawContent,
		String tenantId,
		String[] documentTypes,
		String indexName,
		String errorMessage) {
}
