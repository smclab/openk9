/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.datasource.event.config;

import io.openk9.datasource.event.processor.EventProcessor;
import io.openk9.datasource.event.sender.EventSender;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.opensearch.client.IndicesClient;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.PutComposableIndexTemplateRequest;
import org.opensearch.client.indices.PutMappingRequest;
import org.opensearch.cluster.metadata.ComposableIndexTemplate;
import org.opensearch.cluster.metadata.Template;
import org.opensearch.common.compress.CompressedXContent;
import org.opensearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.List;

@Dependent
@Startup
public class EventIndexInitializer {

	@Inject
	RestHighLevelClient restHighLevelClient;

	@Inject
	EventConfig config;

	@Inject
	io.quarkus.qute.Template mappings;

	@Inject
	Logger logger;

	@ConfigProperty(name = "io.openk9.events.enabled", defaultValue = "false")
	boolean eventsEnabled;

	@Produces
	@Dependent
	public EventProcessor getEventProcessor(
		EventSender eventSender) {

		if (eventsEnabled) {

			return new EventProcessor(
				eventSender, Logger.getLogger(EventProcessor.class));

		}

		return null;
	}

	@PostConstruct
	public void init() throws IOException {

		if (!eventsEnabled) {

			logger.info("Skipping event-index-template creation.");

			return;

		}

		String indexName = config.getIndexName();

		IndicesClient indices = restHighLevelClient.indices();

		PutMappingRequest putMappingRequest =
			new PutMappingRequest(indexName);

		putMappingRequest.source(
			this.mappings.render(), XContentType.JSON);

		PutComposableIndexTemplateRequest putComposableIndexTemplateRequest =
			new PutComposableIndexTemplateRequest();

		ComposableIndexTemplate composableIndexTemplate =
			new ComposableIndexTemplate(
				List.of(config.getIndexName()),
				new Template(null, new CompressedXContent(mappings.render()), null),
				null, null, null, null);

		putComposableIndexTemplateRequest
			.name("event-index-template")
			.indexTemplate(composableIndexTemplate);

		indices.putIndexTemplate(putComposableIndexTemplateRequest, RequestOptions.DEFAULT);

		logger.info("Created index template " + indexName);

	}

}
