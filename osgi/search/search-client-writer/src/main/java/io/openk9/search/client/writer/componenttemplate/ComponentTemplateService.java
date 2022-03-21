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

package io.openk9.search.client.writer.componenttemplate;

import io.openk9.search.client.api.RestHighLevelClientProvider;
import io.openk9.search.client.api.indextemplate.IndexTemplateService;
import org.elasticsearch.client.ClusterClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.PutComponentTemplateRequest;
import org.elasticsearch.cluster.metadata.ComponentTemplate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component(immediate = true, service = ComponentTemplateService.class)
public class ComponentTemplateService {

	public void createOrUpdateComponentTemplate(
		String componentTemplateName, ComponentTemplate componentTemplate) {

		ClusterClient cluster = _restHighLevelClientProvider.get().cluster();

		try {

			PutComponentTemplateRequest componentTemplateRequest =
				new PutComponentTemplateRequest();

			componentTemplateRequest
				.name(componentTemplateName)
				.componentTemplate(componentTemplate);

			cluster.putComponentTemplate(
				componentTemplateRequest, RequestOptions.DEFAULT);

		}
		catch (IOException e) {
			if (_log.isErrorEnabled()) {
				_log.error(e.getMessage(), e);
			}
		}
	}

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private RestHighLevelClientProvider _restHighLevelClientProvider;

	private static final Logger _log = LoggerFactory.getLogger(
		IndexTemplateService.class);

}
