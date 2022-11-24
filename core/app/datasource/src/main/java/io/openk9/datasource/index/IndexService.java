package io.openk9.datasource.index;

import io.smallrye.mutiny.Uni;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetMappingsRequest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;

@ApplicationScoped
public class IndexService {

	public Uni<Map<String, Object>> getMappings(String indexName) {
		return Uni
			.createFrom()
			.item(() -> {
				try {
					return client.indices().getMapping(
						new GetMappingsRequest().indices(indexName),
						RequestOptions.DEFAULT
					);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			})
			.map(response -> response.mappings().get(indexName).sourceAsMap());
	}

	public Uni<String> getSettings(String indexName) {
		return Uni
			.createFrom()
			.item(() -> {
				try {
					return client.indices().getSettings(
						new GetSettingsRequest().indices(indexName),
						RequestOptions.DEFAULT
					);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			})
			.map(response -> response.getIndexToSettings().get(indexName).toString());
	}

	@Inject
	RestHighLevelClient client;

}
