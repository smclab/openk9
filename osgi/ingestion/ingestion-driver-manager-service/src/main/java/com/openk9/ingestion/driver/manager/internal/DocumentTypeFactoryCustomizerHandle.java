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

package com.openk9.ingestion.driver.manager.internal;

import com.openk9.ingestion.driver.manager.api.DocumentTypeFactory;
import com.openk9.ingestion.driver.manager.api.DocumentTypeFactory.DefaultDocumentTypeFactory;
import com.openk9.ingestion.driver.manager.api.DocumentTypeFactoryCustomizer;
import com.openk9.osgi.util.AutoCloseables;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.function.BiFunction;

@Component(
	immediate = true,
	service = DocumentTypeFactoryCustomizerHandle.class
)
public class DocumentTypeFactoryCustomizerHandle {

	@Activate
	public void activate(BundleContext bundleContext) {

		Sinks.Many<DefaultDocumentTypeFactory> documentTypeFactoryBus =
			Sinks.many().unicast().onBackpressureBuffer();

		Sinks.Many<DocumentTypeFactoryCustomizer>
			documentTypeFactoryCustomizerBus =
				Sinks.many().unicast().onBackpressureBuffer();

		ServiceTracker<DocumentTypeFactory, DocumentTypeFactory>
			documentTypeFactoryServiceTracker = new ServiceTracker<>(
			bundleContext, DocumentTypeFactory.class,
			_createServiceTrackerCustomizer(
				bundleContext,
				(t, tServiceReference) -> {
					if (t instanceof DefaultDocumentTypeFactory) {
						return (DefaultDocumentTypeFactory) t;
					}
					return DefaultDocumentTypeFactory.of(
						(String)tServiceReference.getProperty(
							DocumentTypeFactory.PLUGIN_DRIVER_NAME),
						_objToBoolean(
							tServiceReference
								.getProperty(DocumentTypeFactory.DEFAULT)),
						t.getDocumentType()
					);
				},
				documentTypeFactoryBus));

		documentTypeFactoryServiceTracker.open();

		ServiceTracker<DocumentTypeFactoryCustomizer, DocumentTypeFactoryCustomizer>
			documentTypeFactoryCustomizerServiceTracker = new ServiceTracker<>(
			bundleContext, DocumentTypeFactoryCustomizer.class,
			_createServiceTrackerCustomizer(
				bundleContext,
				documentTypeFactoryCustomizerBus));

		documentTypeFactoryCustomizerServiceTracker.open();

		Flux<DefaultDocumentTypeFactory> documentTypeFactoryFlux =
			documentTypeFactoryBus
				.asFlux()
				.cache();

		Flux<DocumentTypeFactoryCustomizer> documentTypeFactoryCustomizerFlux =
			documentTypeFactoryCustomizerBus
				.asFlux();

		Disposable subscribe = documentTypeFactoryCustomizerFlux
			.flatMap(dtfc ->
				documentTypeFactoryFlux
					.flatMap(dtf -> Mono.fromRunnable(() -> dtfc.accept(dtf))))
			.subscribe();

		_autoClosableSafe = AutoCloseables.mergeAutoCloseableToSafe(
			documentTypeFactoryServiceTracker::close,
			documentTypeFactoryCustomizerServiceTracker::close,
			subscribe::dispose,
			() -> documentTypeFactoryBus.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST),
			() -> documentTypeFactoryCustomizerBus.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST)
		);


	}

	@Deactivate
	public void deactivate() {
		_autoClosableSafe.close();
	}

	private boolean _objToBoolean(Object obj) {

		if (obj == null) {
			return false;
		}

		if (obj instanceof Boolean) {
			return(boolean)obj;
		}

		if (obj instanceof String) {
			return Boolean.parseBoolean((String)obj);
		}

		return false;

	}

	private <T> ServiceTrackerCustomizer<T, T> _createServiceTrackerCustomizer(
		BundleContext context, Sinks.Many<T> sink) {

		return _createServiceTrackerCustomizer(
			context, (t, tServiceReference) -> t, sink);

	}

	private <T, R> ServiceTrackerCustomizer<T, T>
		_createServiceTrackerCustomizer(
			BundleContext context,
			BiFunction<T, ServiceReference<T>, R> function,
			Sinks.Many<R> sink) {

		return new ServiceTrackerCustomizer<T, T>() {
			@Override
			public T addingService(ServiceReference<T> reference) {

				T service = context.getService(reference);

				sink.emitNext(
					function.apply(service, reference),
					Sinks.EmitFailureHandler.FAIL_FAST);

				return service;
			}

			@Override
			public void modifiedService(
				ServiceReference<T> reference, T service) {

				removedService(reference, service);

				addingService(reference);

			}

			@Override
			public void removedService(
				ServiceReference<T> reference, T service) {

				context.ungetService(reference);

			}
		};
	}

	private AutoCloseables.AutoCloseableSafe _autoClosableSafe;

}
