package io.openk9.api.aggregator.client.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Builder
@RegisterForReflection
public class EnrichItemDTO {
    private Long enrichItemId;
    private Integer _position;
    private Boolean active;
    private String jsonConfig;
    private Long enrichPipelineId;
    private String name;
    private String serviceName;
}