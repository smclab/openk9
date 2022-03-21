package io.openk9.auth;

import io.openk9.auth.api.AuthVerifier;
import io.openk9.auth.api.UserInfo;
import io.openk9.http.util.HttpUtil;
import io.openk9.json.api.JsonFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;

import java.util.Base64;

@Component(
	immediate = true,
	service = AuthVerifier.class
)
public class AuthVerifierImpl implements AuthVerifier {

	@Override
	public Mono<UserInfo> getUserInfo(
		HttpServerRequest httpRequest) {
		return getUserInfo(
			httpRequest, Mono.just(HttpUtil.getHostName(httpRequest)));
	}

	@Override
	public Mono<UserInfo> getUserInfo(
		HttpServerRequest httpRequest, Mono<String> nameSupplier) {

		return Mono.defer(() -> {

			String token = httpRequest.requestHeaders().get("Authorization");

			if (token == null || token.isEmpty()) {
				return Mono.empty();
			}

			String[] chunks = token.split("\\.");

			String payloadBase64 = chunks[1];

			Base64.Decoder decoder = Base64.getDecoder();

			byte[] decode = decoder.decode(payloadBase64);

			UserInfo userInfo = _jsonFactory.fromJson(decode, UserInfo.class);

			return Mono.just(userInfo);

		});

	}
	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private JsonFactory _jsonFactory;

}
