package io.openk9.k8sclient.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PodResponse {
	private String serviceName;
	private String status;
	private String podName;
}
