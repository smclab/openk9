package io.openk9.entity.manager.pub.sub.binding.internal;

import io.openk9.ingestion.api.Binding;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

@Component(
	immediate = true,
	service = Binding.class
)
public class EntityManagerResponseBinding implements Binding {

	@interface Config {
		String exchange() default "entity-manager-response.direct";
		Exchange.Type type() default Exchange.Type.direct;
		String routingKey() default "#";
	}

	@Activate
	void activate(Config config) {
		_exchange = Exchange.of(config.exchange(), config.type());
		_routingKey = config.routingKey();
	}

	@Modified
	void modified(Config config) {
		activate(config);
	}

	@Override
	public Exchange getExchange() {
		return _exchange;
	}

	@Override
	public String getRoutingKey() {
		return _routingKey;
	}

	private Exchange _exchange;
	private String _routingKey;

}
