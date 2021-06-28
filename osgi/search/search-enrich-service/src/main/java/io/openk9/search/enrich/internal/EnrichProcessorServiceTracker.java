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

package io.openk9.search.enrich.internal;

import io.openk9.cbor.api.CBORFactory;
import io.openk9.ingestion.api.Binding;
import io.openk9.ingestion.api.BundleReceiver;
import io.openk9.ingestion.api.BundleSender;
import io.openk9.ingestion.api.BundleSenderProvider;
import io.openk9.json.api.ObjectNode;
import io.openk9.model.EnrichItem;
import io.openk9.osgi.util.AutoCloseables;
import io.openk9.search.enrich.api.EndEnrichProcessor;
import io.openk9.search.enrich.api.EnrichProcessor;
import io.openk9.search.enrich.api.dto.EnrichProcessorContext;
import lombok.RequiredArgsConstructor;
import org.apache.felix.dm.DependencyManager;
import org.apache.felix.dm.ServiceDependency;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
class EnrichProcessorServiceTracker
	implements ServiceTrackerCustomizer<EnrichProcessor, EnrichProcessor> {

	@Override
	public EnrichProcessor addingService(
		ServiceReference<EnrichProcessor> reference) {

		EnrichProcessor service = _bundleContext.getService(reference);

		Binding binding = Binding.of(exchange, service.name(), service.name());

		ServiceRegistration<Binding> serviceRegistration =
			_bundleContext.registerService(
				Binding.class, binding, new Hashtable<>());

		EnrichProcessorExtension enrichProcessorExtension =
			new EnrichProcessorExtension(
				reference.getBundle(), service, prefetch);

		enrichProcessorExtension.start();

		_map.put(
			service, AutoCloseables.mergeAutoCloseableToSafe(
				serviceRegistration::unregister,
				enrichProcessorExtension::destroy
			)
		);

		return service;
	}

	@Override
	public void modifiedService(
		ServiceReference<EnrichProcessor> reference, EnrichProcessor service) {

		removedService(reference, service);

		addingService(reference);

	}

	@Override
	public void removedService(
		ServiceReference<EnrichProcessor> reference, EnrichProcessor service) {

		AutoCloseables.AutoCloseableSafe autoCloseableSafe = _map.get(service);

		if (autoCloseableSafe != null) {
			autoCloseableSafe.close();
		}

		_bundleContext.ungetService(reference);

	}

	public static class EnrichProcessorExtension {

		public void destroy() {
			_dependencyManager.remove(_component);
		}

		public void start() {
			_dependencyManager.add(_component);
		}

		private EnrichProcessorExtension(
			Bundle bundle, EnrichProcessor enrichProcessor, int prefetch) {

			_dependencyManager = new DependencyManager(
				bundle.getBundleContext());

			_component = _dependencyManager.createComponent();

			_component.setImplementation(
				new EnrichProcessorInitializer(prefetch, enrichProcessor));

			ServiceDependency bundleReceiverDependency =
				_dependencyManager.createServiceDependency()
					.setRequired(true)
					.setService(
						BundleReceiver.class,
						"(queue=" + enrichProcessor.name() + ")");

			ServiceDependency bundleSenderProviderDependency =
				_dependencyManager.createServiceDependency()
					.setRequired(true)
					.setService(BundleSenderProvider.class);

			ServiceDependency cborFactoryDependency=
				_dependencyManager.createServiceDependency()
					.setRequired(true)
					.setService(CBORFactory.class);

			ServiceDependency endEnrichProcessorDependency =
				_dependencyManager.createServiceDependency()
					.setRequired(true)
					.setService(EndEnrichProcessor.class);

			_component.add(
				bundleReceiverDependency, bundleSenderProviderDependency,
				cborFactoryDependency, endEnrichProcessorDependency);

		}

		private final org.apache.felix.dm.Component _component;
		private final DependencyManager _dependencyManager;

	}

	@RequiredArgsConstructor
	public static class EnrichProcessorInitializer {

		public void start() {

			_disposable = m_bundleReceiver
				.consumeAutoAck(prefetch)
				.flatMap(delivery -> {

					EnrichProcessorContext context =
						m_cborFactory.fromCBOR(
							delivery.getBody(), EnrichProcessorContext.class);

					List<String> dependencies = context.getDependencies();

					ObjectNode objectNode = m_cborFactory
						.treeNode(context.getObjectNode())
						.toObjectNode();

					EnrichItem enrichItem = context
						.getDatasourceContext()
						.getEnrichItems()
						.stream()
						.filter(e -> e.getServiceName().equals(_enrichProcessor.name()))
						.findFirst().orElseThrow(IllegalStateException::new);

					return _enrichProcessor.process(
						objectNode,
						context.getDatasourceContext(),
						enrichItem,
						context.getPluginDriverDTO()
					)
						.flatMap(
							jsonNode -> {

								if (dependencies.isEmpty()) {
									return m_endEnrichProcessor
										.exec(
											EnrichProcessorContext.of(
												jsonNode.toMap(),
												context.getDatasourceContext(),
												context.getPluginDriverDTO(),
												Collections.emptyList()
											)
										);
								}

								LinkedList<String> linkedList =
									new LinkedList<>(dependencies);

								String routingKey = linkedList.pop();

								BundleSender bundleSender =
									m_bundleSenderProvider
										.getBundleSender(routingKey);

								if (bundleSender == null) {
									throw new IllegalStateException(
										"bundleSender for routingKey: " +
										routingKey + " not exist");
								}

								EnrichProcessorContext newContext =
									EnrichProcessorContext.of(
										jsonNode.toMap(),
										context.getDatasourceContext(),
										context.getPluginDriverDTO(),
										linkedList
									);

								return bundleSender.send(
									Mono.just(m_cborFactory.toCBOR(newContext))
								);

							})
						.onErrorContinue((throwable, o) -> {

							if (_log.isErrorEnabled()) {

								if (o != null) {
									_log.error("error on object: " + o, throwable);
								}
								else {
									_log.error(throwable.getMessage(), throwable);
								}
							}

						});
				})
				.subscribe();

		}

		public void stop() {
			_disposable.dispose();
		}

		private volatile BundleSenderProvider m_bundleSenderProvider;
		private volatile BundleReceiver m_bundleReceiver;
		private volatile CBORFactory m_cborFactory;
		private volatile EndEnrichProcessor m_endEnrichProcessor;

		private Disposable _disposable;

		private final int prefetch;
		private final EnrichProcessor _enrichProcessor;

	}

	private final int prefetch;

	private final Binding.Exchange exchange;

	private final BundleContext _bundleContext;

	private final Map<EnrichProcessor, AutoCloseables.AutoCloseableSafe> _map =
		new IdentityHashMap<>();

	private static final Logger _log = LoggerFactory.getLogger(
		EnrichProcessorServiceTracker.class
	);

}
