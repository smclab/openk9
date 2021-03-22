package io.openk9.relationship.graph.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor(staticName = "of")
@Builder
public class Request {
	private List<String> labels;
	private Map<String, Object> properties;
}
