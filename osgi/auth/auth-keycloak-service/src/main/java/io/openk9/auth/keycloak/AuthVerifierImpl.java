package io.openk9.auth.keycloak;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import io.openk9.auth.keycloak.api.AuthVerifier;
import io.openk9.auth.keycloak.api.KeycloakClient;
import io.openk9.auth.keycloak.api.UserInfo;
import io.openk9.http.util.HttpUtil;
import io.openk9.ingestion.api.Binding;
import io.openk9.ingestion.api.Delivery;
import io.openk9.ingestion.api.ReceiverReactor;
import io.openk9.json.api.JsonFactory;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.index.qual.NonNegative;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.server.HttpServerRequest;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

@Component(
	immediate = true,
	service = AuthVerifier.class
)
public class AuthVerifierImpl implements AuthVerifier {

	@Activate
	public void activate() {
		_userTokenUserInfoAsyncLoadingCache = Caffeine
			.newBuilder()
			.expireAfter(new Expiry<UserToken, UserInfo>() {

				@Override
				public long expireAfterCreate(
					UserToken key, UserInfo value,
					long currentTime) {
					return Duration.ofSeconds(value.getExp())
						.minus(
							Duration.ofMillis(System.currentTimeMillis()))
						.toNanos();

				}

				@Override
				public long expireAfterUpdate(
					UserToken key, UserInfo value,
					long currentTime, @NonNegative long currentDuration) {
					return currentDuration;
				}

				@Override
				public long expireAfterRead(
					UserToken key, UserInfo value,
					long currentTime, @NonNegative long currentDuration) {
					return currentDuration;
				}
			})
			.removalListener((key, value, cause) -> {
				if (_log.isDebugEnabled()) {
					_log.debug(
						"key: " + key + ", value: " +
						value + ", cause: " + cause);
				}
			})
			.buildAsync((key, executor) -> _keycloakClient
				.introspect(key.virtualHost, key.token)
				.subscribeOn(Schedulers.fromExecutor(executor))
				.toFuture()
			);

		_disposable = _receiver
			.consumeAutoAck(_binding.getQueue())
			.map(Delivery::getBody)
			.map(bytes -> _jsonFactory.fromJson(bytes, KeycloakEvent.class))
			.filter(keycloakEvent -> keycloakEvent.getError() == null)
			.subscribe(this::_handle);

		CaffeineCacheMetrics.monitor(
			Metrics.globalRegistry,
			_userTokenUserInfoAsyncLoadingCache,
			"auth",
			Collections.emptyList()
		);

	}

	private void _handle(KeycloakEvent keycloakEvent) {

		LoadingCache<UserToken, UserInfo> synchronous =
			_userTokenUserInfoAsyncLoadingCache
				.synchronous();

		Action _action = _NOTHING;

		for (Map.Entry<UserToken, UserInfo> entry : synchronous.asMap().entrySet()) {

			UserToken key = entry.getKey();

			if (!key.virtualHost.equals(keycloakEvent.getRealmId())) {
				continue;
			}

			UserInfo value = entry.getValue();

			if (!value.getClientId().equals(keycloakEvent.getClientId())) {
				continue;
			}

			if (!value.getSub().equals(keycloakEvent.getUserId())) {
				continue;
			}

			if (!value.getSessionState().equals(keycloakEvent.getSessionId())) {
				continue;
			}

			switch (keycloakEvent.getType()) {
				case "LOGIN":
				case "REFRESH_TOKEN":
				case "UPDATE":
				case "LOGOUT":
					_action = _action.andThen(
						() -> synchronous.invalidate(key));
					break;
			}

		}

		_action.exec();

	}

	@Deactivate
	public void deactivate() {
		_userTokenUserInfoAsyncLoadingCache.synchronous().invalidateAll();
		_disposable.dispose();
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

		String token = httpRequest.requestHeaders().get("Authorization");

		if (token == null || token.isEmpty()) {
			return Mono.just(GUEST);
		}

		return
			nameSupplier
				.flatMap(name ->
					Mono.fromFuture(
						_userTokenUserInfoAsyncLoadingCache
							.get(UserToken.of(name, token.substring(7))))
						.defaultIfEmpty(GUEST)
				);
	}

	private AsyncLoadingCache<UserToken, UserInfo>
		_userTokenUserInfoAsyncLoadingCache;

	@Data
	@RequiredArgsConstructor(staticName = "of")
	private static class UserToken {
		private final String virtualHost;
		private final String token;
	}

	private interface Action {
		void exec();

		default Action andThen(Action action) {
			return () -> {this.exec(); action.exec();};
		}

	}

	private Disposable _disposable;

	@Reference
	private KeycloakClient _keycloakClient;

	@Reference
	private ReceiverReactor _receiver;

	@Reference(target = "(component.name=io.openk9.auth.keycloak.AuthBinding)")
	private Binding _binding;

	@Reference
	private JsonFactory _jsonFactory;

	private static final Action _NOTHING = () -> {};

	private static final Logger _log =
		LoggerFactory.getLogger(AuthVerifierImpl.class);

}
