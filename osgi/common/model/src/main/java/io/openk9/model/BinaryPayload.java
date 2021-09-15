package io.openk9.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class BinaryPayload {
	private String id;
	private String name;
	private String contentType;
	private String data;
}
