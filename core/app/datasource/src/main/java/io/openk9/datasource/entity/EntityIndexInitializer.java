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

package io.openk9.datasource.entity;

import io.quarkus.runtime.StartupEvent;
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

import java.io.IOException;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class EntityIndexInitializer {

	public void init(@Observes StartupEvent event) throws IOException {

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
