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

package com.openk9.search.client.internal.indextemplate;

import com.openk9.search.client.api.RestHighLevelClientProvider;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.ComposableIndexTemplateExistRequest;
import org.elasticsearch.client.indices.PutComposableIndexTemplateRequest;
import org.elasticsearch.cluster.metadata.ComposableIndexTemplate;
import org.elasticsearch.cluster.metadata.Template;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@Component(immediate = true, service = IndexTemplateService.class)
public class IndexTemplateService {

	public void createOrUpdateIndexTemplate(
		String indexTemplateName, String settings, List<String> indexPatterns,
		String mappings, List<String> componentTemplates, long priority) {

		Template template;

		Settings settingsElasticsearch = null;

		if (!(settings == null || settings.isEmpty())) {
			settingsElasticsearch =
				Settings
					.builder()
					.loadFromSource(settings, XContentType.JSON)
					.build();
		}

		try {
			template = new Template(
				settingsElasticsearch,
				new CompressedXContent(mappings), null);
		}
		catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		ComposableIndexTemplate composableIndexTemplate =
			new ComposableIndexTemplate(
				indexPatterns, template, componentTemplates, priority, null, null);

		createOrUpdateIndexTemplate(
			indexTemplateName, composableIndexTemplate);

	}


	public void createOrUpdateIndexTemplate(
		String indexTemplateName, String settings, List<String> indexPatterns,
		String mappings, long priority) {

		createOrUpdateIndexTemplate(
			indexTemplateName, settings, indexPatterns, mappings, null,
			priority);

	}

	public void createOrUpdateIndexTemplate(
		String indexTemplateName, List<String> indexPatterns, String mappings,
		long priority) {

		createOrUpdateIndexTemplate(
			indexTemplateName, null, indexPatterns, mappings, priority);

	}

	public void createOrUpdateIndexTemplate(
		String indexTemplateName,
		ComposableIndexTemplate composableIndexTemplate) {

		ComposableIndexTemplateExistRequest indexTemplateExistRequest =
			new ComposableIndexTemplateExistRequest(indexTemplateName);

		IndicesClient indices = _restHighLevelClientProvider.get().indices();

		try {

			PutComposableIndexTemplateRequest indexTemplateRequest =
				new PutComposableIndexTemplateRequest();

			indexTemplateRequest
				.name(indexTemplateName)
				.indexTemplate(composableIndexTemplate);

			indices.putIndexTemplate(
				indexTemplateRequest, RequestOptions.DEFAULT);

		}
		catch (IOException e) {
			if (_log.isErrorEnabled()) {
				_log.error(e.getMessage(), e);
			}
		}
	}

	@Reference
	private RestHighLevelClientProvider _restHighLevelClientProvider;

	private static final Logger _log = LoggerFactory.getLogger(
		IndexTemplateService.class);

}
