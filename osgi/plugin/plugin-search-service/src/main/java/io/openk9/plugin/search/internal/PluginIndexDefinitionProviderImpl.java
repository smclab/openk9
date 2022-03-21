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

package io.openk9.plugin.search.internal;

import io.openk9.plugin.api.PluginIndexDefinition;
import io.openk9.plugin.api.PluginIndexDefinitionProvider;
import io.openk9.search.client.api.indextemplate.IndexTemplateService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.Optional;

@Component(
	immediate = true,
	service = PluginIndexDefinitionProvider.class
)
public class PluginIndexDefinitionProviderImpl implements
	PluginIndexDefinitionProvider {

	@Activate
	public void activate(BundleContext bundleContext) {
		_pluginIndexDefinitionServiceTracker =
			new PluginIndexDefinitionServiceTracker(
				bundleContext, _indexTemplateService);

		_pluginIndexDefinitionServiceTracker.open();
	}

	@Deactivate
	public void deactivate() {
		_pluginIndexDefinitionServiceTracker.close();
	}


	@Override
	public List<PluginIndexDefinition> getPluginIndexDefinitionList() {
		return _pluginIndexDefinitionServiceTracker.getPluginIndexDefinition();
	}

	@Override
	public Optional<PluginIndexDefinition> getPluginIndexDefinition(
		String driverName) {

		return getPluginIndexDefinitionList()
			.stream()
			.filter(definition -> driverName.equals(definition.getIndexName()))
			.findFirst();

	}

	private PluginIndexDefinitionServiceTracker
		_pluginIndexDefinitionServiceTracker;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private IndexTemplateService _indexTemplateService;

}
