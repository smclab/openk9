package io.openk9.auth.api;

import io.openk9.search.api.query.QueryParser;
import org.elasticsearch.index.query.BoolQueryBuilder;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public interface ACLQueryContributor
	extends BiFunction<
		UserInfo, QueryParser.Context, Mono<Consumer<BoolQueryBuilder>>> {

	ACLQueryContributor NOTHING =
		(userInfo, context) -> Mono.just((ignore) -> {});

	Mono<Consumer<BoolQueryBuilder>> NOTHING_CONSUMER =
		Mono.just((ignore) -> {});

}
