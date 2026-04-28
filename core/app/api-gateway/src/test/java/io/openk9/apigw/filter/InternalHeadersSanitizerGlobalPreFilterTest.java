/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.apigw.filter;

import java.util.concurrent.atomic.AtomicReference;

import io.openk9.common.util.web.InternalHeaders;

import org.junit.jupiter.api.Test;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

class InternalHeadersSanitizerGlobalPreFilterTest {

	private final InternalHeadersSanitizerGlobalPreFilter filter = 	
		new InternalHeadersSanitizerGlobalPreFilter();

	@Test
	void should_remove_all_internal_headers_and_preserve_non_internal() {
		// setup
		var request = MockServerHttpRequest.get("/test")
			.header(InternalHeaders.TENANT_ID, "spoofed-tenant")
			.header(InternalHeaders.ROLES, "spoofed-roles")
			.header(InternalHeaders.ACL, "spoofed-acl")
			.header("X-CUSTOM", "custom-value")
			.build();

		ServerWebExchange exchange = 
			MockServerWebExchange.from(request);

		AtomicReference<ServerWebExchange> capturedExchange = 
			new AtomicReference<>();

		GatewayFilterChain chain = ex -> {
			capturedExchange.set(ex);
			return Mono.empty();
		};

		// action
		filter.filter(exchange, chain).block();

		// assertion
		HttpHeaders headers = 
			capturedExchange.get().getRequest().getHeaders();

		assertThat(headers.get(InternalHeaders.TENANT_ID)).isNull();
		assertThat(headers.get(InternalHeaders.ROLES)).isNull();
		assertThat(headers.get(InternalHeaders.ACL)).isNull();
		assertThat(headers.get("X-CUSTOM"))
			.singleElement()
			.isEqualTo("custom-value");
	}

}
