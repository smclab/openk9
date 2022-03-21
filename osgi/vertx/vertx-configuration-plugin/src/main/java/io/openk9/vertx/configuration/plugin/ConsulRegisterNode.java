package io.openk9.vertx.configuration.plugin;

import io.vertx.ext.consul.CheckOptions;
import io.vertx.ext.consul.ConsulClient;
import io.vertx.ext.consul.ServiceOptions;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@Component(
	immediate = true,
	service = {
		ConsulRegisterNode.class, Supplier.class
	},
	property = {
		"service.name=this",
		"consul.config.disabled=true"
	}
)
public class ConsulRegisterNode implements Supplier<String> {

	@Override
	public String get() {
		return _name;
	}

	@Activate
	void activate() {

		Map<String, String> getenv = System.getenv();

		String address =
			getenv.getOrDefault("CONSUL_SERVICE_ADDRESS", "localhost");

		_name = getenv.getOrDefault("CONSUL_SERVICE_NAME", address);

		_nodeId = _name + "-" + UUID.randomUUID();

		int port = Integer.parseInt(
			getenv.getOrDefault("CONSUL_SERVICE_PORT", "8080"));

		ServiceOptions options = new ServiceOptions();

		options.setName(_name);
		options.setId(_nodeId);
		options.setAddress(address);
		options.setPort(port);
		options.setCheckOptions(
			new CheckOptions()
				.setTlsSkipVerify(Boolean.parseBoolean(getenv.getOrDefault("CONSUL_SERVICE_CHECK_TLS_SKIP_VERIFY", "false")))
				.setHttp(getenv.getOrDefault("CONSUL_SERVICE_CHECK_URL", ""))
				.setTtl(getenv.getOrDefault("CONSUL_SERVICE_CHECK_TTL", null))
				.setInterval(getenv.getOrDefault("CONSUL_SERVICE_CHECK_INTERVAL", null))
		);

		_consulClient
			.registerService(options)
			.toCompletionStage()
			.toCompletableFuture()
			.join();

	}

	@Deactivate
	void deactivate() {
		_consulClient
			.deregisterService(_nodeId)
			.toCompletionStage()
			.toCompletableFuture()
			.join();
	}

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private ConsulClient _consulClient;

	private volatile String _name;

	private String _nodeId;

}
