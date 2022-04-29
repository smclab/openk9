package io.openk9.auth.api;

import org.elasticsearch.index.query.BoolQueryBuilder;

import java.util.Objects;
import java.util.function.BiConsumer;

public interface ACLQueryContributor extends BiConsumer<UserInfo, BoolQueryBuilder> {

	default String driverServiceName() {
		return this.getClass().getName();
	}

	default ACLQueryContributor andThen(ACLQueryContributor after) {
		Objects.requireNonNull(after);

		return (l, r) -> {
			accept(l, r);
			after.accept(l, r);
		};

	}

	ACLQueryContributor NOTHING = (userInfo, booleanClauses) -> {};

}
