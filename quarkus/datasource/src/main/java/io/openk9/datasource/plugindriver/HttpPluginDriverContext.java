package io.openk9.datasource.plugindriver;


import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.openk9.datasource.jackson.serializer.TemporalAccessorToMillisecondsSerializer;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@RegisterForReflection
public class HttpPluginDriverContext {
	private long datasourceId;
	private String tenantId;
	private String scheduleId;
	@JsonSerialize(using = TemporalAccessorToMillisecondsSerializer.class)
	private OffsetDateTime timestamp;
	private Map<String, Object> datasourceConfig = new HashMap<>();

	@JsonAnySetter
	public void addRest(String key, Object value) {
		datasourceConfig.put(key, value);
	}

	@JsonAnyGetter
	public Map<String, Object> getRest() {
		return datasourceConfig;
	}

}
