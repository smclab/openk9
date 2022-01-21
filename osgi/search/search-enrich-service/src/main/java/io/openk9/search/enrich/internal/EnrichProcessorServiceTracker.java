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

import io.openk9.ingestion.api.Binding;
import io.openk9.ingestion.api.BundleReceiver;
import io.openk9.ingestion.api.BundleSender;
import io.openk9.ingestion.api.BundleSenderProvider;
import io.openk9.json.api.ArrayNode;
import io.openk9.json.api.JsonFactory;
import io.openk9.json.api.JsonNode;
import io.openk9.json.api.ObjectNode;
import io.openk9.model.DatasourceContext;
import io.openk9.model.EnrichItem;
import io.openk9.model.IngestionPayload;
import io.openk9.osgi.util.AutoCloseables;
import io.openk9.plugin.driver.manager.model.PluginDriverDTO;
import io.openk9.search.enrich.api.AsyncEnrichProcessor;
import io.openk9.search.enrich.api.EndEnrichProcessor;
import io.openk9.search.enrich.api.EnrichProcessor;
import io.openk9.search.enrich.api.SyncEnrichProcessor;
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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
class EnrichProcessorServiceTracker
	implements ServiceTrackerCustomizer<EnrichProcessor, EnrichProcessor> {

	@Override
	public EnrichProcessor addingService(
		ServiceReference<EnrichProcessor> reference) {

		List<AutoCloseable> autoCloseableSafes =
			new ArrayList<>();

		EnrichProcessor service = _bundleContext.getService(reference);

		Binding binding = Binding.of(exchange, service.name(), service.name());

		ServiceRegistration<Binding> serviceRegistration =
			_bundleContext.registerService(
				Binding.class, binding, new Hashtable<>());

		autoCloseableSafes.add(serviceRegistration::unregister);

		if (service instanceof AsyncEnrichProcessor) {

			AsyncEnrichProcessor asyncEnrichProcessor =
				((AsyncEnrichProcessor)service);

			Binding bindingDestinationName = Binding.of(
				exchange,
				asyncEnrichProcessor.destinationName(),
				asyncEnrichProcessor.destinationName());

			ServiceRegistration<Binding> destinationNameServiceRegistration =
				_bundleContext.registerService(
					Binding.class, bindingDestinationName, new Hashtable<>());

			autoCloseableSafes.add(
				destinationNameServiceRegistration::unregister);
		}

		EnrichProcessorExtension enrichProcessorExtension =
			new EnrichProcessorExtension(
				reference.getBundle(), service, prefetch);

		enrichProcessorExtension.start();

		autoCloseableSafes.add(enrichProcessorExtension::destroy);

		_map.put(
			service, AutoCloseables.mergeAutoCloseableToSafe(
				autoCloseableSafes
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
					.setService(JsonFactory.class);

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

					ObjectNode context =
						m_jsonFactory.fromJsonToJsonNode(
							delivery.getBody()).toObjectNode();

					ArrayNode dependencies = context.get("dependencies").toArrayNode();

					ObjectNode objectNode = context.get("payload").toObjectNode();

					IngestionPayload ingestionPayload =
						m_jsonFactory.fromJson(objectNode.toString(),
							IngestionPayload.class);

					boolean isValid = _enrichProcessor.validate(ingestionPayload);

					JsonNode datasourceContextJson =
						context.get("datasourceContext");

					DatasourceContext datasourceContext =
						m_jsonFactory.fromJson(
							datasourceContextJson.toString(),
							DatasourceContext.class);

					JsonNode pluginDriverJson = context.get("pluginDriver");

					PluginDriverDTO pluginDriver = m_jsonFactory.fromJson(
						pluginDriverJson.toString(),
						PluginDriverDTO.class
					);

					EnrichItem enrichItem = datasourceContext
						.getEnrichItems()
						.stream()
						.filter(e -> e.getServiceName().equals(_enrichProcessor.name()))
						.findFirst().orElseThrow(IllegalStateException::new);

					String routingKey;

					if (dependencies.isEmpty()) {
						routingKey = "index-writer";
					}
					else {
						routingKey =
							dependencies.remove(0).asText();
					}

					if (_enrichProcessor instanceof AsyncEnrichProcessor) {

						String destinationName = isValid
							? ((AsyncEnrichProcessor)_enrichProcessor).destinationName()
							: routingKey;

						BundleSender bundleSender =
							m_bundleSenderProvider.getBundleSender(
								destinationName);

						ObjectNode ob = m_jsonFactory.createObjectNode();

						ob.put("payload", objectNode);
						ob.put(
							"enrichItemConfig",
							m_jsonFactory.fromJsonToJsonNode(
								enrichItem.getJsonConfig()
							)
						);
						ob.put("datasourceContext", datasourceContextJson);
						ob.put("pluginDriver", pluginDriverJson);

						if (routingKey.equals("index-writer")) {
							ob.put("dependencies", m_jsonFactory.createArrayNode());
						}
						else {
							ob.put("dependencies", dependencies);
						}

						ob.put("replyTo", routingKey);

						return bundleSender.send(
							Mono.just(ob.toString().getBytes())
						);

					}
					else if (_enrichProcessor instanceof SyncEnrichProcessor) {

						return (
							isValid
								? ((SyncEnrichProcessor)_enrichProcessor).process(
									objectNode,
									datasourceContext,
									enrichItem,
									pluginDriver
								)
								: Mono.just(objectNode)
						)
							.flatMap(
								jsonNode -> {

									ObjectNode ob2 =
										m_jsonFactory.createObjectNode();

									ob2.put("payload", jsonNode);
									ob2.put("datasourceContext", m_jsonFactory.fromObjectToJsonNode(datasourceContext));
									ob2.put("pluginDriver", m_jsonFactory.fromObjectToJsonNode(pluginDriver));

									if (dependencies.isEmpty()) {

										ob2.put(
											"dependencies",
											m_jsonFactory.createArrayNode());

										return m_endEnrichProcessor.exec(ob2);
									}

									ob2.put("replyTo", routingKey);

									ob2.put(
										"dependencies",
										dependencies);

									BundleSender bundleSender =
										m_bundleSenderProvider
											.getBundleSender(routingKey);

									if (bundleSender == null) {
										throw new IllegalStateException(
											"bundleSender for routingKey: " +
											routingKey + " not exist");
									}

									return bundleSender.send(
										Mono.just(ob2.toString().getBytes())
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
					}
					else {
						return Mono.error(() -> new IllegalStateException(
							"EnrichProcessor instanceof must be AsyncEnrichProcessor or SyncEnrichProcessor"));
					}

				})
				.subscribe();

		}

		public void stop() {
			_disposable.dispose();
		}

		private volatile BundleSenderProvider m_bundleSenderProvider;
		private volatile BundleReceiver m_bundleReceiver;
		private volatile JsonFactory m_jsonFactory;
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
