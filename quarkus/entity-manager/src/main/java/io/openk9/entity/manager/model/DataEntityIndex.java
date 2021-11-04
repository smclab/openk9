package io.openk9.entity.manager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
public class DataEntityIndex {
	private final long id;
	private final String entityType;
	private final List<String> context;
}
