package io.openk9.ingestion.api;

public interface AcknowledgableMessage<T> extends AcknowledgableDelivery {
	T getMessage();
}
