package io.openk9.entity.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class EntityManagerDataPayload {

	private DataPayload payload;
	private String replyTo;
	private Map<String, Object> enrichItemConfig;

}
