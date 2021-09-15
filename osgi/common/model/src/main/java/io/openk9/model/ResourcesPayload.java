package io.openk9.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class ResourcesPayload {
	private List<BinaryPayload> binaries;
}
