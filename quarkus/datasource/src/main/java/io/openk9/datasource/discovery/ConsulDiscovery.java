package io.openk9.datasource.discovery;

import io.openk9.datasource.config.ServiceDiscoveryRegistrationConfig;
import io.vertx.ext.consul.ConsulClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.consul.ConsulClient;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class ConsulDiscovery {

	@Produces
	ConsulClient create(ServiceDiscoveryRegistrationConfig consulConfig, Vertx vertx) {

		String[] split = consulConfig.getConsulHostPort().split(":");

		ConsulClientOptions consulClientOptions;

		if (split.length == 1) {
			consulClientOptions = new ConsulClientOptions()
				.setHost(split[0]);
		}
		else {
			consulClientOptions = new ConsulClientOptions()
				.setHost(split[0])
				.setPort(Integer.parseInt(split[1]));
		}

		consulClientOptions.setSsl(!consulConfig.isServiceSkipTlsVerify());

		return ConsulClient.create(vertx, consulClientOptions);

	}


}
