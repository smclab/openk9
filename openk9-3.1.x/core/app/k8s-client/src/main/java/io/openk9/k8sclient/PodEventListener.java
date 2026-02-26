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
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class PodEventListener {

	private BroadcastProcessor<Pod> podProcessor;

	@PreDestroy
	public void destroy() {
		podWatcher.close();
	}

	@PostConstruct
	public void init() {

		ListOptions listOptions = new ListOptions();
		listOptions.setWatch(true);
		listOptions.setLimit(10000L);

		// TODO Review the implementation, that now remove reactor and use only mutiny
		podProcessor = BroadcastProcessor.create();

		podWatcher = kubernetesClient
			.pods()
			.inNamespace(namespace)
			.watch(listOptions, new Watcher<>() {
				@Override
				public void eventReceived(Action action, Pod pod) {
					podProcessor.onNext(pod);
				}

				@Override
				public void onClose(WatcherException e) {
					podProcessor.onError(e);
				}

				@Override
				public void onClose() {
					podProcessor.onComplete();
				}

			});

	}

	@Inject
	KubernetesClient kubernetesClient;

	@Inject
	@ConfigProperty(name = "quarkus.kubernetes.namespace")
	String namespace;

	private Watch podWatcher;

	public Multi<Pod> getPods() {
		return podProcessor;
	}

}
