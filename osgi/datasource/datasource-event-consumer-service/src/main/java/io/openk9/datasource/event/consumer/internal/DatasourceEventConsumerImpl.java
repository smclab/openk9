package io.openk9.datasource.event.consumer.internal;

import io.openk9.datasource.event.consumer.api.Constants;
import io.openk9.datasource.event.consumer.api.DatasourceEventConsumer;
import io.openk9.ingestion.api.Delivery;
import io.openk9.ingestion.api.ReceiverReactor;
import io.openk9.json.api.JsonFactory;
import io.openk9.model.Datasource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import reactor.core.publisher.Flux;

@Component(
	immediate = true,
	service = DatasourceEventConsumer.class
)
public class DatasourceEventConsumerImpl implements DatasourceEventConsumer {

	@Override
	public Flux<Datasource> datasourceUpdateEvents() {
		return _receiverReactor.consumeAutoAck(
			Constants.QUEUE_PREFIX + Constants.UPDATE + "-" + DATASOURCE, 1)
			.transform(this::_deserialize);
	}

	@Override
	public Flux<Datasource> datasourceInsertEvents() {
		return _receiverReactor.consumeAutoAck(
			Constants.QUEUE_PREFIX + Constants.INSERT + "-" + DATASOURCE)
			.transform(this::_deserialize);
	}

	@Override
	public Flux<Datasource> datasourceDeleteEvents() {
		return _receiverReactor.consumeAutoAck(
			Constants.QUEUE_PREFIX + Constants.DELETE + "-" + DATASOURCE)
			.transform(this::_deserialize);
	}

	private Flux<Datasource> _deserialize(Flux<Delivery> deliveryFlux) {
		return deliveryFlux.map(
			delivery -> _jsonFactory.fromJson(
				delivery.getBody(), Datasource.class)
		);
	}

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private JsonFactory _jsonFactory;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private ReceiverReactor _receiverReactor;

	private static final String DATASOURCE = Datasource.class.getSimpleName();

}
