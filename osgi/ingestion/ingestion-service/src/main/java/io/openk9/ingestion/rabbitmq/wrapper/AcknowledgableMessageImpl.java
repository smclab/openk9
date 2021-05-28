package io.openk9.ingestion.rabbitmq.wrapper;

import io.openk9.ingestion.api.AcknowledgableMessage;
import reactor.rabbitmq.AcknowledgableDelivery;

public class AcknowledgableMessageImpl<T>
	extends AcknowledgableDeliveryWrapper implements AcknowledgableMessage<T> {

	public AcknowledgableMessageImpl(
		AcknowledgableDelivery delegate, T message) {
		super(delegate);
		_message = message;
	}

	@Override
	public T getMessage() {
		return _message;
	}

	private T _message;

}
