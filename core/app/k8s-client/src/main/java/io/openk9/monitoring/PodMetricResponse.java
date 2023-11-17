package io.openk9.monitoring;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PodMetricResponse {
	private String podName;
	private String cpuUsage;
	private String ramUsage;
}
