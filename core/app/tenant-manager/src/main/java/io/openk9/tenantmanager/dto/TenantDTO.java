package io.openk9.tenantmanager.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Data
public class TenantDTO {
	@NotBlank
	@NotNull
	private String schemaName;
	@NotBlank
	@NotNull
	private String virtualHost;
	@NotBlank
	@NotNull
	private String clientId;
	private String clientSecret;
	@NotBlank
	@NotNull
	private String realmName;
}
