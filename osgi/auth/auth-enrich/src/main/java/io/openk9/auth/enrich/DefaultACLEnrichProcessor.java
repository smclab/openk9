package io.openk9.auth.enrich;

import io.openk9.json.api.JsonFactory;
import io.openk9.json.api.JsonNode;
import io.openk9.json.api.ObjectNode;
import io.openk9.model.DatasourceContext;
import io.openk9.model.EnrichItem;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.search.enrich.api.EnrichProcessor;
import io.openk9.search.enrich.api.SyncEnrichProcessor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import reactor.core.publisher.Mono;

@Component(
	immediate = true,
	service = EnrichProcessor.class
)
public class DefaultACLEnrichProcessor implements SyncEnrichProcessor {

	@Override
	public String name() {
		return DefaultACLEnrichProcessor.class.getName();
	}

	@Override
	public Mono<ObjectNode> process(
		ObjectNode objectNode, DatasourceContext datasourceContext,
		EnrichItem enrichItem, PluginDriverDTO pluginDriverDTO) {

		return Mono.fromSupplier(() -> {

			JsonNode aclNode = objectNode.get("acl");

			JsonNode payload = objectNode.get("payload");

			ObjectNode aclIndexNode = payload
				.toObjectNode()
				.putObject("acl");

			if (aclNode == null || aclNode.isEmpty()) {

				aclIndexNode.put("public", true);

			}
			else {

				String pluginName = pluginDriverDTO.getName();

				ObjectNode aclPluginIndexNode =
					aclIndexNode.putObject(pluginName);

				aclPluginIndexNode.putAll(aclNode.toObjectNode());

			}

			return objectNode;

		});


	}

	@Reference
	private JsonFactory _jsonFactory;

}
