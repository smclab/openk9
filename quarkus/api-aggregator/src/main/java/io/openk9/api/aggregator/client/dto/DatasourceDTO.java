package io.openk9.api.aggregator.client.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Builder
@RegisterForReflection
public class DatasourceDTO {
    private Long datasourceId;
    private Boolean active;
    private String description;
    private String jsonConfig;
    private Instant lastIngestionDate;
    private String name;
    private Long tenantId;
    private String scheduling;
    private String driverServiceName;
}