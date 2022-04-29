package io.openk9.auth.query.parser;

import io.openk9.search.api.query.QueryParser;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import reactor.core.publisher.Mono;

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

		return _addAclQueryParser(context.getAclQuery());
	}

	private Mono<Consumer<BoolQueryBuilder>> _addAclQueryParser(
		String pluginAclQuery) {

		return Mono
			.fromSupplier(() ->
				boolQueryBuilder -> {

					QueryBuilder aclQueryBuilder =
						_createAclQuery(pluginAclQuery);

					boolQueryBuilder
						.filter(
							QueryBuilders
								.boolQuery()
								.minimumShouldMatch(1)
								.should(QueryBuilders.matchQuery("acl.public", true))
								.should(aclQueryBuilder)
						);


			}
		);

	}

	private QueryBuilder _createAclQuery(String pluginAclQuery) {

		if (pluginAclQuery == null || pluginAclQuery.isBlank()) {
			return null;
		}
		else {
			return _deserializePluginAclQuery(pluginAclQuery);
		}

	}

	private QueryBuilder _deserializePluginAclQuery(String pluginAclQuery) {
		return QueryBuilders.wrapperQuery(pluginAclQuery);
	}

	private boolean _enabled;

}
