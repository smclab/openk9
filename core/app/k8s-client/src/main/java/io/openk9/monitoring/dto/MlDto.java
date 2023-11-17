package io.openk9.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MlDto {

	private String pipelineName;
	private String modelName;
	private String tokenizerName;
	private String library;
}

