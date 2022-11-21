package io.openk9.datasource.entity;

import io.quarkus.runtime.Startup;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.PutComposableIndexTemplateRequest;
import org.elasticsearch.cluster.metadata.ComposableIndexTemplate;
import org.elasticsearch.common.xcontent.DeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.io.IOException;

@Dependent
@Startup
public class EntityIndexInitializer {

	@PostConstruct
	public void init() throws IOException {

		IndicesClient indices = client.indices();

		try (XContentParser parser = XContentType
			.JSON.xContent()
			.createParser(
				NamedXContentRegistry.EMPTY,
				DeprecationHandler.THROW_UNSUPPORTED_OPERATION,
				this.entitymappings.render())) {

			ComposableIndexTemplate entityIndexTemplate =
				ComposableIndexTemplate.parse(parser);

			PutComposableIndexTemplateRequest putComposableIndexTemplateRequest =
				new PutComposableIndexTemplateRequest();
			
			putComposableIndexTemplateRequest
				.name("entity-index-template")
				.indexTemplate(entityIndexTemplate);

			indices.putIndexTemplate(
				putComposableIndexTemplateRequest, RequestOptions.DEFAULT);

			logger.info("Created index template entity-index-template");
			
		}

	}

	@Inject
	RestHighLevelClient client;

	@Inject
	io.quarkus.qute.Template entitymappings;

	@Inject
	Logger logger;

}
