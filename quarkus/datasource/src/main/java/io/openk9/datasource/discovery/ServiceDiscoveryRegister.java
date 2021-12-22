package io.openk9.datasource.discovery;

import io.openk9.datasource.config.ServiceDiscoveryRegistrationConfig;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.vertx.ext.consul.CheckList;
import io.vertx.ext.consul.CheckOptions;
import io.vertx.ext.consul.ServiceOptions;
import io.vertx.mutiny.ext.consul.ConsulClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class ServiceDiscoveryRegister {

	void onStart(@Observes StartupEvent ev) {
		ScheduledExecutorService executorService = Executors
			.newSingleThreadScheduledExecutor();
		executorService.schedule(() -> {
			CheckList instances =
				consulClient.healthChecksAndAwait(consulConfig.getServiceName());

			int size = instances != null ? instances.getList().size() : 0;

			String checkUrl = String.format(
				"%s://%s:%s%s",
				consulConfig.getServiceSchema(),
				consulConfig.getServiceAddress(),
				consulConfig.getServicePort(),
				consulConfig.getServiceCheckPath());

			instanceId = consulConfig.getServiceName() + "-" + size;
			ServiceOptions registration = new ServiceOptions()
				.setId(instanceId)
				.setName(consulConfig.getServiceName())
				.setAddress(consulConfig.getServiceAddress())
				.setPort(consulConfig.getServicePort())
				.setMeta(Map.of("version", consulConfig.getServiceVersion()))
				.setCheckOptions(
					new CheckOptions()
						.setTlsSkipVerify(consulConfig.isServiceSkipTlsVerify())
						.setHttp(checkUrl)
						.setInterval(consulConfig.getServiceInterval())
				);
			consulClient.registerServiceAndAwait(registration);
			logger.info("Instance registered: id=" + registration.getId());
		}, 5000, TimeUnit.MILLISECONDS);
	}

	void onStop(@Observes ShutdownEvent ev) {
		consulClient.deregisterServiceAndAwait(instanceId);
		logger.info("Instance de-registered: id=" +  instanceId);
	}

	@Inject
	ConsulClient consulClient;

	@Inject
	ServiceDiscoveryRegistrationConfig consulConfig;

	@Inject
	Logger logger;

	private String instanceId;


}
