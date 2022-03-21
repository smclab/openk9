package io.openk9.auth.common;

import java.util.function.Function;

public interface ACLQueryContributor extends Function<UserInfo, String> {
}
