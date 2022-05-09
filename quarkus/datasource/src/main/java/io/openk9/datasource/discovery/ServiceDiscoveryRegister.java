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
