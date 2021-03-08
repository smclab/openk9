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

package io.openk9.search.client.internal.plugin;


import io.openk9.search.client.internal.indextemplate.IndexTemplateService;
import io.openk9.plugin.api.PluginIndexDefinition;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PluginIndexDefinitionServiceTracker implements
	ServiceTrackerCustomizer<PluginIndexDefinition, PluginIndexDefinition> {

	public PluginIndexDefinitionServiceTracker(
		BundleContext bundleContext,
		IndexTemplateService indexTemplateService) {
		_bundleContext = bundleContext;
		_indexTemplateService = indexTemplateService;
	}

	public void open() {
		_serviceTracker = new ServiceTracker<>(
			_bundleContext, PluginIndexDefinition.class, this);

		_serviceTracker.open();
	}

	public void close () {
		_serviceTracker.close();
	}

	@Override
	public PluginIndexDefinition addingService(
		ServiceReference<PluginIndexDefinition> reference) {

		PluginIndexDefinition service = _bundleContext.getService(reference);

		_handleAddingService(service);

		return service;
	}

	@Override
	public void modifiedService(
		ServiceReference<PluginIndexDefinition> reference,
		PluginIndexDefinition service) {

		removedService(reference, service);

		addingService(reference);

	}

	@Override
	public void removedService(
		ServiceReference<PluginIndexDefinition> reference,
		PluginIndexDefinition service) {

		_handleRemovedService(service);

		_bundleContext.ungetService(reference);

	}

	public List<PluginIndexDefinition> getPluginIndexDefinition() {
		return new ArrayList<>(
			Arrays.asList(_serviceTracker.getServices(_DEFAULT_ARRAY)));
	}

	private void _handleAddingService(PluginIndexDefinition service) {

		String indexName = service.getIndexName();

		String indexTemplateName = indexName + "_template";

		List<String> indexPatterns = Collections.singletonList(
			"*-" + indexName + "-data");

		_indexTemplateService.createOrUpdateIndexTemplate(
			indexTemplateName, service.getSettings(), indexPatterns,
			service.getMapping(), service.componentTemplates(), 10);

	}

	private void _handleRemovedService(PluginIndexDefinition service) {
		// ignore
	}

	private ServiceTracker<PluginIndexDefinition, PluginIndexDefinition>
		_serviceTracker;

	private final IndexTemplateService _indexTemplateService;

	private final BundleContext _bundleContext;

	private static final PluginIndexDefinition[] _DEFAULT_ARRAY =
		new PluginIndexDefinition[0];

}
