package io.openk9.datasource.processor.util;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@ToString
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor(staticName = "of")
@RegisterForReflection
public class Field {
	private String name;
	private String type;
	private String subName;
	private String subType;
}
