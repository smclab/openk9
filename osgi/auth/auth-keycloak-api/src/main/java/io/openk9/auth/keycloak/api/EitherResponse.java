package io.openk9.auth.keycloak.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EitherResponse<L, R> {
	private L response;
	private R rest;
}
