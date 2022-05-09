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

package io.openk9.auth;

import io.openk9.auth.api.AuthVerifier;
import io.openk9.auth.api.UserInfo;
import io.openk9.http.util.HttpUtil;
import io.openk9.json.api.JsonFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;

import java.util.Base64;

@Component(
	immediate = true,
	service = AuthVerifier.class
)
public class AuthVerifierImpl implements AuthVerifier {

	@Override
	public UserInfo getUserInfo_(HttpServerRequest httpRequest) {

		String token = httpRequest.requestHeaders().get("Authorization");

		if (token == null || token.isEmpty()) {
			return null;
		}

		String[] chunks = token.split("\\.");

		String payloadBase64 = chunks[1];

		Base64.Decoder decoder = Base64.getDecoder();

		byte[] decode = decoder.decode(payloadBase64);

		return _jsonFactory.fromJson(decode, UserInfo.class);

	}

	@Override
	public Mono<UserInfo> getUserInfo(
		HttpServerRequest httpRequest) {
		return getUserInfo(
			httpRequest, Mono.just(HttpUtil.getHostName(httpRequest)));
	}

	@Override
	public Mono<UserInfo> getUserInfo(
		HttpServerRequest httpRequest, Mono<String> nameSupplier) {

		return Mono.justOrEmpty(getUserInfo_(httpRequest));

	}
	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private JsonFactory _jsonFactory;

}
