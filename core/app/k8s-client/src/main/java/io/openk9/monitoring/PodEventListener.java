package io.openk9.monitoring;

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
	@ConfigProperty(name = "openk9.kubernetes-client.namespace")
	String namespace;

	private Watch podWatcher;
	private Watch serviceWatcher;

	private Sinks.Many<Service> serviceProcessor;

	private Sinks.Many<Pod> podProcessor;


}
