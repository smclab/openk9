package io.openk9.datasource.processor.payload;

import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class EnrichPipelinePayload {
	private DataPayload payload;
	private Map<String, Object> enrichItemConfig;
	private String replyTo;

	public JsonObject toJson() {
		return JsonObject.mapFrom(this);
	}

}