package io.openk9.http.web;

import reactor.netty.http.server.HttpServerRoutes;

public interface RouterHandler {

	HttpServerRoutes handle(HttpServerRoutes router);

	default RouterHandler compose(RouterHandler before) {
		return router -> this.handle(before.handle(router));
	}

	default RouterHandler andThen(RouterHandler after) {
		return router -> after.handle(this.handle(router));
	}

	RouterHandler NOTHING = a -> a;

}
