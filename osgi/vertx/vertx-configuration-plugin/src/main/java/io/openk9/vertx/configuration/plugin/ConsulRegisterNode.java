package io.openk9.vertx.configuration.plugin;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.util.Map;
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
	}

	@Deactivate
	void deactivate() {
		_name = null;
	}

	private volatile String _name;

}
