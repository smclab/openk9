package io.openk9.datasource.client;

import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

@Dependent
public class WebClientFactory {
	@Produces
	public WebClient createHttpClient(Vertx vertx) {
		return WebClient.create(vertx);
	}
}
