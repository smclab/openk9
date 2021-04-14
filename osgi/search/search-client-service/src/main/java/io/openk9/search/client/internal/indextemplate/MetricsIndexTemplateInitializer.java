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

package io.openk9.search.client.internal.indextemplate;

import io.openk9.search.client.api.indextemplate.IndexTemplateService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, enabled = true)
public class MetricsIndexTemplateInitializer extends
	BaseIndexTemplateInitializer {

	@Activate
	public void activate(BundleContext bundleContext) {
		super.activate(bundleContext);
	}

	@Override
	public String indexTemplatePath() {
		return "mappings/base-metrics-index-template.json";
	}

	@Override
	public String indexTemplateName() {
		return "base_metrics_template";
	}

	@Override
	@Reference
	public void setIndexTemplateService(
		IndexTemplateService indexTemplateService) {
		super.setIndexTemplateService(indexTemplateService);
	}

}
