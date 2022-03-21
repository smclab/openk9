package io.openk9.auth.query.parser;

import io.openk9.auth.api.ACLQueryContributor;
import io.openk9.auth.api.AuthVerifier;
import io.openk9.auth.api.UserInfo;
import io.openk9.search.api.query.QueryParser;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Component(
	immediate = true,
	service = QueryParser.class
)
public class AuthQueryParser implements QueryParser {

	@interface Config {
		boolean enabled() default true;
	}

	@Activate
	@Modified
	void activate(Config config) {
		_enabled = config.enabled();
	}

	@Override
	public Mono<Consumer<BoolQueryBuilder>> apply(Context context) {

		if (!_enabled) {
			return NOTHING.apply(context);
		}

		return Mono.defer(() ->
			_authVerifier
				.getUserInfo(context.getHttpRequest())
				.defaultIfEmpty(AuthVerifier.GUEST)
				.flatMap(userInfo -> _addAclQueryParser(userInfo, context))
		);
	}

	private Mono<Consumer<BoolQueryBuilder>> _addAclQueryParser(
		UserInfo userInfo, QueryParser.Context context) {

		if (userInfo == AuthVerifier.GUEST) {
			return QueryParser.NOTHING_CONSUMER;
		}

		return Mono.defer(() -> {

			List<Mono<Consumer<BoolQueryBuilder>>> consumers =
				new ArrayList<>(_aclQueryParsers.size());

			for (ACLQueryContributor aclQueryParser : _aclQueryParsers) {
				consumers.add(
					aclQueryParser.apply(userInfo, context)
				);
			}

			return Mono.zip(consumers, (objs) -> {

				Consumer<BoolQueryBuilder> boolQueryBuilderConsumer = _NOTHING;

				for (Object obj : objs) {
					boolQueryBuilderConsumer = boolQueryBuilderConsumer.andThen(
						(Consumer<? super BoolQueryBuilder>)obj);
				}

				return boolQueryBuilderConsumer;

			});
		});

	}

	private boolean _enabled;

	@Reference(
		policyOption = ReferencePolicyOption.GREEDY,
		cardinality = ReferenceCardinality.AT_LEAST_ONE
	)
	private List<ACLQueryContributor> _aclQueryParsers;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private AuthVerifier _authVerifier;

	private static final Consumer<BoolQueryBuilder> _NOTHING = ignore -> {};

}
