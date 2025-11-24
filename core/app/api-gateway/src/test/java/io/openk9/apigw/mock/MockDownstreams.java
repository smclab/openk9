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

package io.openk9.apigw.mock;

import java.text.ParseException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

/**
 * Provides simple in-process HTTP servers for demonstration purposes.
 * <p>
 * These mock downstream services are started on random ports,
 * and return static JSON responses. They are used only to illustrate how the
 * API Gateway routes traffic to backend services in this PoC.
 */
@Slf4j
@Configuration
public class MockDownstreams {

	private static final ObjectMapper objMapper = new ObjectMapper();

	private static String json(String serviceName, String tenantId, SignedJWT signedJWT) {
		Map<String, Object> mapJson = Map.of(
			"data", "hello from %s".formatted(serviceName),
			"tenantId", tenantId != null ? tenantId : "unknown",
			"jwt", signedJWT
		);

		try {
			return objMapper.writeValueAsString(mapJson);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@Bean(destroyMethod = "dispose")
	DisposableServer datasourceServer() {

		return HttpServer.create()
			.handle((req, res) -> response(req, res, "datasource"))
			.bind()
			.block();
	}

	@Bean
	int datasourcePort(DisposableServer datasourceServer) {
		return datasourceServer.port();
	}

	@Bean(destroyMethod = "dispose")
	DisposableServer searcherServer() {

		return HttpServer.create()
			.handle((req, res) -> response(req, res, "searcher"))
			.bind()
			.block();
	}

	@Bean
	int searcherPort(DisposableServer searcherServer) {
		return searcherServer.port();
	}

	private static  Publisher<Void> response(
		HttpServerRequest req, HttpServerResponse res, String downstream) {

		res.header("Content-Type", "application/json");

		var headers = req.requestHeaders();
		SignedJWT jwt = null;

		if (!headers.contains(HttpHeaders.AUTHORIZATION)) {

			return res.status(400).send();
		}
		String authorization = headers.get(HttpHeaders.AUTHORIZATION);

		if (!StringUtils.startsWithIgnoreCase(authorization, "bearer")) {
			return res.status(400).send();
		}
		String jwtString = authorization.substring(7);

		try {
			jwt = SignedJWT.parse(jwtString);
		}
		catch (ParseException e) {
			return res.status(400).send();
		}

		var tenantId = headers.get("X-TENANT-ID");

		return res
			.header("Content-Type", "application/json")
			.sendString(Mono.just(json(downstream, tenantId, jwt)));
	}
}
