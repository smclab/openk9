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

package io.openk9.datasource.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

@Getter
@NoArgsConstructor
@ApplicationScoped
public class ServiceDiscoveryRegistrationConfig {

	@ConfigProperty(name = "quarkus.application.version")
	String serviceVersion;

	@ConfigProperty(name = "service.discovery.registration.service.schema", defaultValue = "http")
	String serviceSchema;

	@ConfigProperty(name = "service.discovery.registration.service.check.path", defaultValue = "/q/health")
	String serviceCheckPath;

	@ConfigProperty(name = "service.discovery.registration.service.address", defaultValue = "localhost")
	String serviceAddress;

	@ConfigProperty(name = "quarkus.application.name")
	String serviceName;

	@ConfigProperty(name = "service.discovery.registration.service.port", defaultValue = "8080")
	int servicePort;

	@ConfigProperty(name = "service.discovery.registration.service.interval", defaultValue = "15s")
	String serviceInterval;

	@ConfigProperty(name = "service.discovery.registration.service.skip.tls.verify", defaultValue = "true")
	boolean serviceSkipTlsVerify;

	@ConfigProperty(name = "quarkus.consul-config.agent.host-port", defaultValue = "localhost:8500")
	String consulHostPort;

	@ConfigProperty(name = "quarkus.consul-config.agent.use-https", defaultValue = "false")
	boolean useHttps;

}
