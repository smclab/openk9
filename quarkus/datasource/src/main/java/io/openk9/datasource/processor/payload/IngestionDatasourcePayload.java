package io.openk9.datasource.processor.payload;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@RegisterForReflection
public class IngestionDatasourcePayload {
	private IngestionPayload ingestionPayload;
	private DatasourceContext datasourceContext;
}
