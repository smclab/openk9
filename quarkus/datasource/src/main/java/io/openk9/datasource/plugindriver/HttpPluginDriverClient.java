package io.openk9.datasource.plugindriver;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class HttpPluginDriverClient {

	public Uni<HttpResponse<Buffer>> invoke(
		HttpPluginDriverInfo httpPluginDriverInfo,
		HttpPluginDriverContext httpPluginDriverContext) {

		String path = httpPluginDriverInfo.getPath();

		if (path == null) {
			path = "/invoke";
		}

		HttpPluginDriverInfo.Method httpMethod = httpPluginDriverInfo.getMethod();

		if (httpMethod == null) {
			httpMethod = HttpPluginDriverInfo.Method.POST;
		}

		String host = httpPluginDriverInfo.getHost();

		if (host == null) {
			host = "localhost";
		}

		Integer port = httpPluginDriverInfo.getPort();

		if (port == null || port < 1 || port > 65535) {
			port = 8080;
		}

		return webClient.request(
				httpMethod.getHttpMethod(),
				port,
				host,
				path
			)
			.ssl(httpPluginDriverInfo.isSecure())
			.sendJson(httpPluginDriverContext);
	}

	public void invokeAndForget(
		HttpPluginDriverInfo httpPluginDriverInfo,
		HttpPluginDriverContext httpPluginDriverContext) {

		invoke(httpPluginDriverInfo, httpPluginDriverContext)
			.subscribe()
			.with(
				response -> {
					if (response.statusCode() != 200) {
						logger.warn(
							"response.statusCode() != 200 (" + response.statusCode() + ")" +
							" response.bodyAsString() = " + response.bodyAsString() + " " +
							"response.statusMessage() = " + response.statusMessage());
					}
					else {
						logger.info("invoke success " + httpPluginDriverInfo);
					}
				},
				t -> logger.error("HttpPluginDriverClient.invokeAndForget", t)
			);
	}

	@Inject
	WebClient webClient;

	@Inject
	Logger logger;

}
