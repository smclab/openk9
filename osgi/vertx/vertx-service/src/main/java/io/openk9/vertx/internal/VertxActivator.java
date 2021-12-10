package io.openk9.vertx.internal;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component(
	immediate = true,
	service = VertxActivator.class
)
public class VertxActivator {

	@Activate
	void activate(BundleContext bundleContext, Map<String, Object> config)
		throws IOException {

		_vertx = Vertx.vertx(new VertxOptions(JsonObject.mapFrom(config)));

		_serviceRegistrations.add(
			bundleContext.registerService(
				Vertx.class, _vertx, null));

		_serviceRegistrations.add(
			bundleContext.registerService(
				EventBus.class, _vertx.eventBus(), null));

		_serviceRegistrations.add(
			bundleContext.registerService(
				FileSystem.class, _vertx.fileSystem(), null));

	}

	@Modified
	void modified(BundleContext bundleContext, Map<String, Object> config)
		throws IOException{

		deactivate();
		activate(bundleContext, config);

	}

	@Deactivate
	void deactivate() {

		_vertx.close().toCompletionStage().toCompletableFuture().join();

		Iterator<ServiceRegistration> iterator =
			_serviceRegistrations.iterator();

		while (iterator.hasNext()) {
			ServiceRegistration sr = iterator.next();
			sr.unregister();
			iterator.remove();
		}
	}

	private Vertx _vertx;

	private final List<ServiceRegistration> _serviceRegistrations =
		new ArrayList<>();

	private static final Logger _log = LoggerFactory.getLogger(
		VertxActivator.class);

}
