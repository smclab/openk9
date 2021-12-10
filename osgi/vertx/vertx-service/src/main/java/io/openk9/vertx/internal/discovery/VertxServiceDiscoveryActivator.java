package io.openk9.vertx.internal.discovery;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceDiscoveryOptions;
import io.vertx.servicediscovery.consul.ConsulServiceImporter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import java.util.Map;
import java.util.Objects;

@Component(
	immediate = true,
	service = VertxServiceDiscoveryActivator.class,
	property = {
		"host=consul",
		"port:Integer=8500",
		"scan-period:Integer=2000"
	}
)
public class VertxServiceDiscoveryActivator {

	@Activate
	void activate(Map<String, Object> config, BundleContext context) {

		prevConfig = config;

		JsonObject entries = JsonObject.mapFrom(config);

		_serviceDiscovery =
			ServiceDiscovery.create(
				_vertx, new ServiceDiscoveryOptions(entries));

		_serviceDiscovery.registerServiceImporter(
			new ConsulServiceImporter(), JsonObject.mapFrom(config));

		_serviceRegistration =
			context.registerService(
				ServiceDiscovery.class, _serviceDiscovery, null);

	}

	@Modified
	void modified(Map<String, Object> config, BundleContext context) {
		if (!Objects.equals(prevConfig, config)) {
			deactivate();
			activate(config, context);
		}
	}

	@Deactivate
	void deactivate() {
		try {
			_serviceDiscovery.close();
		}
		catch (Exception e) {
			// ignore
		}
		_serviceDiscovery = null;
		_serviceRegistration.unregister();
		_serviceRegistration = null;
	}

	@Reference
	private Vertx _vertx;

	private ServiceDiscovery _serviceDiscovery;

	private ServiceRegistration _serviceRegistration;

	private transient Map<String, Object> prevConfig = null;

}
