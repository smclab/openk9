package io.openk9.datasource.plugindriver;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class HttpPluginDriverClient {

	public Uni<HttpResponse<Buffer>> invoke(
		HttpPluginDriverInfo httpPluginDriverInfo,
		OffsetDateTime fromDate, long datasourceId, String scheduleId) {

		Map<String, Object> body = new HashMap<>(httpPluginDriverInfo.getBody());
		body.put("timestamp", fromDate.toInstant().toEpochMilli());
		body.put("datasourceId", datasourceId);
		body.put("scheduleId", scheduleId);

		return webClient.request(
				httpPluginDriverInfo.getMethod().getHttpMethod(),
				httpPluginDriverInfo.getPort(),
				httpPluginDriverInfo.getHost(),
				httpPluginDriverInfo.getPath()
			)
			.ssl(httpPluginDriverInfo.isSecure())
			.sendJson(body);
	}

	public void invokeAndForget(
		HttpPluginDriverInfo httpPluginDriverInfo,
		OffsetDateTime fromDate, long datasourceId, String scheduleId) {
		invoke(httpPluginDriverInfo, fromDate, datasourceId, scheduleId)
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
