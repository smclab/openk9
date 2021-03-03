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

package com.openk9.schemaregistry.internal.registry;

import com.openk9.schemaregistry.register.SchemaDefinition;
import com.openk9.schemaregistry.register.SchemaDefinitionListener;
import com.openk9.schemaregistry.register.SchemaDefinitionRegistry;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component(
	immediate = true,
	service = SchemaDefinitionRegistry.class
)
public class SchemaRegistryTracker
	implements SchemaDefinitionRegistry,
		ServiceTrackerCustomizer<SchemaDefinition, SchemaDefinition> {

	@Activate
	public void activate(BundleContext bundleContext) {

		_bundleContext = bundleContext;

		_serviceTracker = new ServiceTracker<>(
			bundleContext, SchemaDefinition.class, this
		);
		_serviceTracker.open();
	}

	@Modified
	public void modified(BundleContext bundleContext) {
		deactivate();
		activate(bundleContext);
	}

	@Deactivate
	public void deactivate() {
		_serviceTracker.close();
		_serviceTracker = null;
		_bundleContext = null;
	}

	@Override
	public List<SchemaDefinition> getSchemaDefinition(String subject) {
		return new ArrayList<>(
			_registry.getOrDefault(subject, Collections.emptyMap()).values());
	}

	@Override
	public SchemaDefinition getSchemaDefinition(
		Integer version, String subject) {
		return _registry
			.getOrDefault(subject, Collections.emptyMap())
			.get(version);
	}

	@Override
	public SchemaDefinition addingService(
		ServiceReference<SchemaDefinition> reference) {

		SchemaDefinition service = _bundleContext.getService(reference);

		for (SchemaDefinitionListener schemaDefinitionListener :
			_schemaDefinitionListeners) {
			schemaDefinitionListener.onCreate(service);
		}

		_registry.compute(
			service.getSubject(),
			(k1, m1) -> {
				m1 = m1 == null ? new HashMap<>() : m1;
				m1.put(service.getVersion(), service);
				return m1;
		});

		return service;
	}

	@Override
	public void modifiedService(
		ServiceReference<SchemaDefinition> reference,
		SchemaDefinition service) {

		removedService(reference, service);

		addingService(reference);

	}

	@Override
	public void removedService(
		ServiceReference<SchemaDefinition> reference,
		SchemaDefinition service) {

		_bundleContext.ungetService(reference);

		for (SchemaDefinitionListener schemaDefinitionListener :
			_schemaDefinitionListeners) {
			schemaDefinitionListener.onRemove(service);
		}

		_registry.compute(
			service.getSubject(), (k1, m1) -> {
				if (m1 == null) {
					return null;
				}

				m1.remove(service.getVersion());
				return m1;
			});

	}

	private Map<String, Map<Integer, SchemaDefinition>> _registry =
		new HashMap<>();

	private BundleContext _bundleContext;

	private ServiceTracker<SchemaDefinition, SchemaDefinition> _serviceTracker;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private List<SchemaDefinitionListener> _schemaDefinitionListeners;

}
