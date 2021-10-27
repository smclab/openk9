package io.openk9.ingestion.rabbitmq.bind;

import io.openk9.ingestion.api.Binding;
import io.openk9.ingestion.api.BindingRegistry;
import io.openk9.osgi.util.AutoCloseables;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Component(
	immediate = true,
	service = BindingRegistry.class
)
public class BindingRegistryImpl implements BindingRegistry {

	@Activate
	public void activate(BundleContext bundleContext) {
		_bundleContext = bundleContext;
	}

	@Override
	public AutoCloseables.AutoCloseableSafe register(
		Binding.Exchange exchange, String routingKey, String queue) {
		Objects.requireNonNull(exchange, "exchange is null");
		Objects.requireNonNull(routingKey, "routingKey is null");
		Objects.requireNonNull(queue, "queue is null");
		return _register(exchange, routingKey, queue);
	}

	@Override
	public AutoCloseables.AutoCloseableSafe register(
		Binding.Exchange exchange, String routingKey) {
		Objects.requireNonNull(exchange, "exchange is null");
		Objects.requireNonNull(routingKey, "routingKey is null");
		return _register(exchange, routingKey, null);
	}

	@Override
	public AutoCloseables.AutoCloseableSafe register(
		Binding.Exchange exchange) {
		Objects.requireNonNull(exchange, "exchange is null");
		return _register(exchange, null, null);
	}

	@Override
	public boolean exists(
		Binding.Exchange exchange, String routingKey, String queue) {
		return _bindings.contains(Binding.of(exchange, routingKey, queue));
	}

	@Override
	public boolean exists(Binding.Exchange exchange, String routingKey) {
		return _bindings.contains(Binding.of(exchange, routingKey, null));
	}

	private AutoCloseables.AutoCloseableSafe _register(
		Binding.Exchange exchange, String routingKey, String queue) {

		Binding binding = Binding.of(exchange, routingKey, queue);

		if (_bindings.contains(binding)) {
			return () -> {};
		}
		else {

			ServiceRegistration<Binding> serviceRegistration =
				_bundleContext.registerService(
					Binding.class,
					binding,
					null
				);

			return AutoCloseables.mergeAutoCloseableToSafe(
				serviceRegistration::unregister);

		}

	}

	private BundleContext _bundleContext;
	private final Set<Binding> _bindings = new HashSet<>();

}
