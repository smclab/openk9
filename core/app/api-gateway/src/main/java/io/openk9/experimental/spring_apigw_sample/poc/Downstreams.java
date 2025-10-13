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

package io.openk9.experimental.spring_apigw_sample.poc;

import java.text.ParseException;

import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

/**
 * Provides simple in-process HTTP servers for demonstration purposes.
 * <p>
 * These mock downstream services are started on random ports,
 * and return static JSON responses. They are used only to illustrate how the
 * API Gateway routes traffic to backend services in this PoC.
 */
@Slf4j
@Configuration
@Profile({"test", "poc", "default"})
public class Downstreams {


	private static final String DATASOURCE_RESPONSE_BODY = """ 
		{
			"data": "hello world from datasource"
		}
		""";

	private static final String SEARCHER_RESPONSE_BODY = """
		{
			"data": "hello world from searcher"
		}
		""";

	@Bean(destroyMethod = "dispose")
	DisposableServer datasourceServer() {

		return HttpServer.create()
			.handle((req, res) -> {
				res.header("Content-Type", "application/json");

				var headers = req.requestHeaders();

				if (headers.contains(HttpHeaders.AUTHORIZATION)) {
					var authorization = headers.get(HttpHeaders.AUTHORIZATION);
					if (StringUtils.startsWithIgnoreCase(authorization, "bearer")) {
						var token = authorization.substring(7);

						SignedJWT jwtToken = null;
						try {
							jwtToken = SignedJWT.parse(token);
							return res.sendString(Mono.just(
								jwtToken.getJWTClaimsSet().toString(false)));
						}
						catch (ParseException e) {
							// ignore
						}
					}
				}

				return res
					.header("Content-Type", "application/json")
					.sendString(Mono.just(DATASOURCE_RESPONSE_BODY));
				}
			)
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
			.handle((req, res) -> res
				.header("Content-Type", "application/json")
				.sendString(Mono.just(SEARCHER_RESPONSE_BODY))
			)
			.bind()
			.block();
	}

	@Bean
	int searcherPort(DisposableServer searcherServer) {
		return searcherServer.port();
	}

}
