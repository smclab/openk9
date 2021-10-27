package io.openk9.entity.manager.pub.sub.binding.internal;

import io.openk9.ingestion.api.Binding;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

@Component(
	immediate = true,
	service = Binding.class
)
public class EntityManagerRequestBinding implements Binding {

	@interface Config {
		String exchange() default "amq.topic";
		Binding.Exchange.Type type() default Binding.Exchange.Type.topic;
		String routingKey() default "entity-manager-request";
		String queue() default "entity-manager-request";
	}

	@Activate
	void activate(Config config) {

		_queue = config.queue();
		_exchange = Binding.Exchange.of(config.exchange(), config.type());
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

	@Override
	public String getQueue() {
		return _queue;
	}

	private String _queue;
	private Exchange _exchange;
	private String _routingKey;

}
