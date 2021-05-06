package io.openk9.plugin.driver.manager.model;

import io.openk9.model.DatasourceContext;
import io.openk9.model.IngestionPayload;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class IngestionDatasourcePluginDriverPayload {
	private IngestionPayload ingestionPayload;
	private DatasourceContext datasourceContext;
	private PluginDriverDTO pluginDriverDTO;
}
