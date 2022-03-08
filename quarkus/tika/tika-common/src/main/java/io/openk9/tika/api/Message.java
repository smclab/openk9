package io.openk9.tika.api;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import io.smallrye.mutiny.Uni;

public interface Message {

	void ackBlock();

	Uni<Void> ack();

	void nackBlock();

	Uni<Void> nack();

	byte[] body();

	Envelope envelope();

	AMQP.BasicProperties properties();

	String consumerTag();

}
