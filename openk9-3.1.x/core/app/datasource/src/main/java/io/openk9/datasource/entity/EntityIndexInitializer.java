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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.opensearch.client.IndicesClient;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.PutComposableIndexTemplateRequest;
import org.opensearch.cluster.metadata.ComposableIndexTemplate;
import org.opensearch.common.xcontent.XContentType;
import org.opensearch.core.xcontent.DeprecationHandler;
import org.opensearch.core.xcontent.NamedXContentRegistry;
import org.opensearch.core.xcontent.XContentParser;

import java.io.IOException;

@ApplicationScoped
public class EntityIndexInitializer {

	@Inject
	RestHighLevelClient restHighLevelClient;

	@Inject
	io.quarkus.qute.Template entitymappings;

	@Inject
	Logger logger;

	@ConfigProperty(name = "io.openk9.entity.index.init", defaultValue = "true")
	boolean indexInit;

	public void init(@Observes StartupEvent event) throws IOException {

		if (!indexInit) {

			logger.info("Skipping entity-index-template creation.");

			return;
		}

		IndicesClient indices = restHighLevelClient.indices();

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


}
