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

package io.openk9.datasource;

import java.io.IOException;

import io.openk9.datasource.index.IndexService;
import io.openk9.datasource.index.model.IndexName;

import org.opensearch.OpenSearchStatusException;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.GetComposableIndexTemplateRequest;
import org.opensearch.client.indices.PutComposableIndexTemplateRequest;
import org.opensearch.cluster.metadata.ComposableIndexTemplate;
import org.opensearch.core.rest.RestStatus;

/**
 * Reads back from OpenSearch the index template a dataIndex creation is
 * expected to have produced.
 */
public class IndexTemplateUtils {

	/**
	 * Retrieves the index template associated with the given dataIndex name.
	 *
	 * @param client         the OpenSearch client
	 * @param tenantId       the tenant the dataIndex belongs to
	 * @param dataIndexName  the name of the dataIndex
	 * @return the index template, or {@code null} when it does not exist
	 */
	public static ComposableIndexTemplate getIndexTemplate(
		RestHighLevelClient client, String tenantId, String dataIndexName) {

		var templateName =
			IndexName.from(tenantId, dataIndexName) + IndexService.TEMPLATE_SUFFIX;

		try {
			var response = client.indices().getIndexTemplate(
				new GetComposableIndexTemplateRequest(templateName),
				RequestOptions.DEFAULT
			);

			return response.getIndexTemplates().get(templateName);
		}
		catch (OpenSearchStatusException exception) {
			if (exception.status() == RestStatus.NOT_FOUND) {
				return null;
			}

			throw new IllegalStateException(exception);
		}
		catch (IOException exception) {
			throw new IllegalStateException(exception);
		}
	}

	/**
	 * Overwrites the index template associated with the given dataIndex name.
	 *
	 * @param client        the OpenSearch client
	 * @param tenantId      the tenant the dataIndex belongs to
	 * @param dataIndexName the name of the dataIndex
	 * @param indexTemplate the index template to store
	 */
	public static void putIndexTemplate(
		RestHighLevelClient client, String tenantId, String dataIndexName,
		ComposableIndexTemplate indexTemplate) {

		var templateName =
			IndexName.from(tenantId, dataIndexName) + IndexService.TEMPLATE_SUFFIX;

		var request = new PutComposableIndexTemplateRequest()
			.name(templateName)
			.indexTemplate(indexTemplate);

		try {
			client.indices().putIndexTemplate(request, RequestOptions.DEFAULT);
		}
		catch (IOException exception) {
			throw new IllegalStateException(exception);
		}
	}

}
