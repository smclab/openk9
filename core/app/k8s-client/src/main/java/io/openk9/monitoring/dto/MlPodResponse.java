package io.openk9.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MlPodResponse {
	private String name;
	private String task;
	private String library;
	private String status;
}
