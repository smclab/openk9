package io.openk9.vertx.configuration.plugin;

import io.vertx.ext.consul.CheckOptions;
import io.vertx.ext.consul.ConsulClient;
import io.vertx.ext.consul.ServiceOptions;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		ServiceOptions options = new ServiceOptions();
		String address =
			getenv.getOrDefault("CONSUL_SERVICE_ADDRESS", "localhost");
		_name = getenv.getOrDefault("CONSUL_SERVICE_NAME", address);
		options.setAddress(address);
		options.setPort(Integer.parseInt(getenv.getOrDefault("CONSUL_SERVICE_PORT", "8080")));
		options.setName(_name);
		options.setCheckOptions(new CheckOptions().setTtl("15s"));
		_nodeId = String.join("-", _name, randomString());
		options.setId(_nodeId);
		_consulClient
			.registerService(options)
			.toCompletionStage()
			.toCompletableFuture().join();
	}

	@Deactivate
	void deactivate() {
		_consulClient
			.deregisterService(_nodeId)
			.toCompletionStage()
			.toCompletableFuture().join();
		_nodeId = null;
		_name = null;
	}

	public String randomString() {
		return UUID.randomUUID().toString();
	}

	private volatile String _nodeId;
	private volatile String _name;

	@Reference
	private ConsulClient _consulClient;

	private static final Logger _log = LoggerFactory.getLogger(
		ConsulRegisterNode.class);

}
