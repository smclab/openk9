package io.openk9.resources.validator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class ResourcesValidatorDataPayload {

	private DataPayload payload;
	private String replyTo;
	private Map<String, Object> enrichItemConfig;

}
