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
