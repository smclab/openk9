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

package io.openk9.k8sclient;

import io.fabric8.kubernetes.api.model.ListOptions;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import reactor.core.publisher.Sinks;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class PodEventListener {

	@PostConstruct
	public void init() {

		ListOptions listOptions = new ListOptions();
		listOptions.setWatch(true);
		listOptions.setLimit(10000L);

		podProcessor = Sinks.unsafe().many().replay().all();

		podWatcher = kubernetesClient
			.pods()
			.inNamespace(namespace)
			.watch(listOptions, new Watcher<>() {
				@Override
				public void eventReceived(Action action, Pod pod) {
					podProcessor.tryEmitNext(pod);

				}

				@Override
				public void onClose(WatcherException e) {
					podProcessor.tryEmitError(e);
				}

				@Override
				public void onClose() {
					podProcessor.tryEmitComplete();
				}

			});

		serviceProcessor = Sinks.unsafe().many().replay().all();

		serviceWatcher = kubernetesClient
			.services()
			.inNamespace(namespace)
			.watch(listOptions, new Watcher<>() {
				@Override
				public void eventReceived(Action action, Service service) {
					serviceProcessor.tryEmitNext(service);

				}

				@Override
				public void onClose(WatcherException e) {
					serviceProcessor.tryEmitError(e);
				}

				@Override
				public void onClose() {
					serviceProcessor.tryEmitComplete();
				}

			});

	}

	@PreDestroy
	public void destroy() {
		podWatcher.close();
		serviceWatcher.close();
	}

	public Multi<Pod> getPods() {
		return Multi.createFrom().publisher(podProcessor.asFlux());
	}

	public Multi<Service> getServices() {
		return Multi.createFrom().publisher(serviceProcessor.asFlux());
	}

	@Inject
	KubernetesClient kubernetesClient;

	@Inject
	@ConfigProperty(name = "quarkus.kubernetes.namespace")
	String namespace;

	private Watch podWatcher;
	private Watch serviceWatcher;

	private Sinks.Many<Service> serviceProcessor;

	private Sinks.Many<Pod> podProcessor;


}
