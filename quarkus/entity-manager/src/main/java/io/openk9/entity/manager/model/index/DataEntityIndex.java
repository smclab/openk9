package io.openk9.entity.manager.model.index;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Collection;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
public class DataEntityIndex {
	private final String id;
	private final String entityType;
	private final Collection<String> context;
}
