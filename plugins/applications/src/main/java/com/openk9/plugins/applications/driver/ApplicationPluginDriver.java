package io.openk9.plugins.applications.driver;

import io.openk9.datasource.model.Datasource;
import io.openk9.ingestion.logic.api.IngestionLogic;
import io.openk9.json.api.ArrayNode;
import io.openk9.json.api.JsonFactory;
import io.openk9.json.api.JsonNode;
import io.openk9.model.IngestionPayload;
import io.openk9.ingestion.driver.manager.api.PluginDriver;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Date;

@Component(
	immediate = true,
	service = PluginDriver.class
)
public class ApplicationPluginDriver implements PluginDriver {

	@interface Config {
		boolean schedulerEnabled() default false;
	}

	@Activate
	public void activate(Config config) {
		_config = config;
	}

	@Modified
	public void modified(Config config) {
		_config = config;
	}

	@Override
	public String getName() {
		return "application";
	}

	@Override
	public Publisher<Void> invokeDataParser(
		Datasource datasource, Date fromDate, Date toDate) {

		JsonNode jsonNode =
			_jsonFactory.fromJsonToJsonNode(datasource.getJsonConfig());

		if (jsonNode.isArray()) {
			ArrayNode arrayJson = jsonNode.toArrayNode();

			for (int i = 0; i < arrayJson.size(); i++) {

				JsonNode node = arrayJson.get(i);

				if (node.hasNonNull("title")) {
					node = node
						.toObjectNode()
						.set("applicationName", node.get("title"));
				}

				_ingestionLogicSender.send(
					IngestionPayload
						.builder()
						.datasourceId(datasource.getDatasourceId())
						.rawContent(node.toString())
						.contentId(Integer.toString(i))
						.tenantId(datasource.getTenantId())
						.datasourcePayload(
							_jsonFactory
								.createObjectNode()
								.set(getName(), node.toObjectNode())
								.toMap()
						)
						.parsingDate(toDate.getTime())
						.type(new String[] {getName()})
						.build()
				);

			}

		}

		return Mono.empty();

	}

	@Override
	public boolean schedulerEnabled() {
		return _config.schedulerEnabled();
	}

	@Reference
	private IngestionLogic _ingestionLogicSender;

	@Reference
	private JsonFactory _jsonFactory;

	private Config _config;



}
