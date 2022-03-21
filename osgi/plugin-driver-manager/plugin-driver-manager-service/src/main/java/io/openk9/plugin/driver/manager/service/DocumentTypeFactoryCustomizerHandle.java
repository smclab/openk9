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

package io.openk9.plugin.driver.manager.service;

import io.openk9.plugin.driver.manager.api.DocumentTypeFactoryCustomizer;
import io.openk9.plugin.driver.manager.api.DocumentTypeProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Component(
	immediate = true,
	service = DocumentTypeFactoryCustomizerHandle.class
)
public class DocumentTypeFactoryCustomizerHandle {

	@Activate
	public void activate(BundleContext bundleContext) {

		_serviceTracker = new ServiceTracker<>(
			bundleContext, DocumentTypeFactoryCustomizer.class,
			new ServiceTrackerCustomizer<>() {
				@Override
				public DocumentTypeFactoryCustomizer addingService(
					ServiceReference<DocumentTypeFactoryCustomizer> reference) {

					DocumentTypeFactoryCustomizer service =
						bundleContext.getService(reference);

					List<Mono<Void>> collect =
						_documentTypeProvider
							.getDocumentTypeMap()
							.entrySet()
							.stream()
							.map(service)
							.collect(Collectors.toList());

					Flux.concat(collect).blockLast(Duration.ofSeconds(10));

					return service;

				}

				@Override
				public void modifiedService(
					ServiceReference<DocumentTypeFactoryCustomizer> reference,
					DocumentTypeFactoryCustomizer service) {

					removedService(reference, service);

					addingService(reference);

				}

				@Override
				public void removedService(
					ServiceReference<DocumentTypeFactoryCustomizer> reference,
					DocumentTypeFactoryCustomizer service) {

					bundleContext.ungetService(reference);

				}
			});

		_serviceTracker.open(true);

	}

	@Deactivate
	public void deactivate() {
		_serviceTracker.close();
	}

	private ServiceTracker<
		DocumentTypeFactoryCustomizer, DocumentTypeFactoryCustomizer>
			_serviceTracker;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private DocumentTypeProvider _documentTypeProvider;

}
