package io.openk9.auth.query.parser;

import io.openk9.auth.api.ACLQueryContributor;
import io.openk9.auth.api.UserInfo;
import io.openk9.search.api.query.QueryParser;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Component(
	immediate = true,
	service = ACLQueryContributor.class
)
public class SampleAclQueryParser implements ACLQueryContributor {

	@Override
	public Mono<Consumer<BoolQueryBuilder>> apply(
		UserInfo userInfo, QueryParser.Context context) {

		if (_log.isDebugEnabled()) {
			_log.debug(userInfo.toString());
		}

		return NOTHING_CONSUMER;
	}

	private static final Logger _log = LoggerFactory.getLogger(
		SampleAclQueryParser.class);

}
