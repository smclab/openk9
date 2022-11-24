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
import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.runtime.Startup;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.PutComposableIndexTemplateRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.cluster.metadata.ComposableIndexTemplate;
import org.elasticsearch.cluster.metadata.Template;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.xcontent.XContentType;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

@Dependent
@Startup
@IfBuildProperty(name = "openk9.events.enabled", stringValue = "true")
public class EventIndexInitializer {

	@PostConstruct
	public void init() throws IOException {

		String indexName = config.getIndexName();

		IndicesClient indices = client.indices();

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

	@Produces
	public EventProcessor getEventProcessor(
		EventSender eventSender) {
		return new EventProcessor(
			eventSender, Logger.getLogger(EventProcessor.class));
	}

	@Inject
	RestHighLevelClient client;

	@Inject
	EventConfig config;

	@Inject
	io.quarkus.qute.Template mappings;

	@Inject
	Logger logger;

}
