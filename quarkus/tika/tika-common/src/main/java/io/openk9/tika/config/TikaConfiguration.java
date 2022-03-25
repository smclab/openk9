package io.openk9.tika.config;

import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

@Getter
@ApplicationScoped
public class TikaConfiguration {

	@ConfigProperty(name = "openk9.tika.ocr.enabled", defaultValue = "false")
	boolean ocrEnabled;

	@ConfigProperty(name = "openk9.tika.ocr.rabbitmq.exchange", defaultValue = "amq.topic")
	String ocrExchange;

	@ConfigProperty(name = "openk9.tika.ocr.rabbitmq.routingkey", defaultValue = "io.openk9.tika.ocr")
	String ocrRoutingKey;

	@ConfigProperty(name = "openk9.tika.ocr.character.length", defaultValue = "10")
	int characterLength;

	@ConfigProperty(name = "openk9.tika.rabbitmq.exchange", defaultValue = "amq.topic")
	String currentExchange;

	@ConfigProperty(name = "openk9.tika.rabbitmq.routingkey")
	String currentRoutingKey;

	@ConfigProperty(name = "openk9.tika.rabbitmq.queue.name")
	String currentQueueName;

	@ConfigProperty(name = "openk9.tika.rabbitmq.x-expires", defaultValue = "3600000")
	String xExpires;

}
