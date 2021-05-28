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
		String exchange() default "entity-manager-response.fanout";
		Exchange.Type type() default Exchange.Type.fanout;
		String routingKey() default "#";
		String queue() default "entity-manager-response";
	}

	@Activate
	@Modified
	void activate(Config config) {
		_exchange = Exchange.of(config.exchange(), config.type());
		_routingKey = config.routingKey();
		_queue = config.queue();
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

	@Override
	public String getQueue() {
		return _queue;
	}

	private Exchange _exchange;
	private String _routingKey;
	private String _queue;

}
