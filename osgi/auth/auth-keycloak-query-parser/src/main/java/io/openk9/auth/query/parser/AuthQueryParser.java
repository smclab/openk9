package io.openk9.auth.query.parser;

import io.openk9.auth.keycloak.api.AuthVerifier;
import io.openk9.auth.keycloak.api.UserInfo;
import io.openk9.datasource.model.Tenant;
import io.openk9.http.web.HttpRequest;
import io.openk9.search.api.query.QueryParser;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component(
	immediate = true,
	service = QueryParser.class
)
public class AuthQueryParser implements QueryParser {

	@Override
	public Mono<Consumer<BoolQueryBuilder>> apply(Context context) {

		if (!_enabled) {
			return NOTHING.apply(context);
		}

		return Mono.defer(() -> {

			HttpRequest httpRequest = context.getHttpRequest();
			Tenant tenant = context.getTenant();

			return _authVerifier
				.getUserInfo(httpRequest)
				.map(userInfo -> bool -> _addAclQuery(tenant, userInfo, bool));

		});
	}

	@interface Config {
		boolean enabled() default true;
	}

	@Activate
	void activate(Config config) {
		_enabled = config.enabled();
	}

	@Modified
	void modified(Config config) {
		_enabled = config.enabled();
	}

	private void _addAclQuery(
		Tenant tenant, UserInfo userInfo,
		BoolQueryBuilder boolQuery) {

		boolQuery.must(
			QueryBuilders
				.matchQuery("acl.allow.roles","Guest")
		);

		if (userInfo == AuthVerifier.GUEST) {
			return;
		}

		for (Map.Entry<String, List<String>> entry : userInfo
			.getRealmAccess()
			.entrySet()) {

			if (entry.getKey().equals(tenant.getVirtualHost())) {
				for (String role : entry.getValue()) {
					boolQuery.must(
						QueryBuilders
							.matchQuery("acl.allow.roles", role));
				}
			}

		}

	}


	private boolean _enabled;

	@Reference
	private AuthVerifier _authVerifier;

}
