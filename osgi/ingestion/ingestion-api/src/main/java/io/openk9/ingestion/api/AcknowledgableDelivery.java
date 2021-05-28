package io.openk9.ingestion.api;

public interface AcknowledgableDelivery extends Delivery {

	void ack();

	void ack(boolean multiple);

	void nack(boolean multiple, boolean requeue);

	void nack(boolean requeue);

}
