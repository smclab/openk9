package io.openk9.datasource.client.plugindriver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Builder
public class SchedulerEnabledDTO {
	private boolean schedulerEnabled;
}
