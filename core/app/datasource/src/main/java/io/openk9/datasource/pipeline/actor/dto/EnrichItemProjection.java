package io.openk9.datasource.pipeline.actor.dto;

import io.openk9.datasource.model.EnrichItem;

public record EnrichItemProjection(
	long datasourceId, long enrichItemId,
	EnrichItem.EnrichItemType enrichItemType,
	String serviceName, String jsonConfig) {
}
