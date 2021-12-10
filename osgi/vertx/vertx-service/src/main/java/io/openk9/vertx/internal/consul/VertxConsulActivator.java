package io.openk9.vertx.internal.consul;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.consul.ConsulClient;
import io.vertx.ext.consul.ConsulClientOptions;
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
	service = VertxConsulActivator.class,
	property = {
		"host=localhost",
		"port:Integer=8500",
		"scan-period:Integer=2000"
	}
)
public class VertxConsulActivator {

	@Activate
	void activate(Map<String, Object> config, BundleContext context) {

		prevConfig = config;

		ConsulClientOptions opts =
			new ConsulClientOptions(JsonObject.mapFrom(config));

		_consulClient = ConsulClient.create(_vertx, opts);

		_serviceRegistration =
			context.registerService(
				ConsulClient.class, _consulClient, null);

	}

	@Modified
	void modified(Map<String, Object> config, BundleContext bundleContext) {
		if (!Objects.equals(prevConfig, config)) {
			deactivate();
			activate(config, bundleContext);
		}
	}

	@Deactivate
	void deactivate() {
		try {
			_consulClient.close();
		}
		catch (Exception e) {
			// ignore
		}
		_consulClient = null;
		_serviceRegistration.unregister();
		_serviceRegistration = null;
	}

	private ConsulClient _consulClient;

	private ServiceRegistration _serviceRegistration;

	@Reference
	private Vertx _vertx;

	private transient Map<String, Object> prevConfig = null;

}
