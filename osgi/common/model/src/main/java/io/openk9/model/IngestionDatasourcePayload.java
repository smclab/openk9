package io.openk9.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class IngestionDatasourcePayload {
	private IngestionPayload ingestionPayload;
	private DatasourceContext datasourceContext;
}
