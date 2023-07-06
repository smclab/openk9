package io.openk9.datasource.pipeline.actor.util;

import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import org.slf4j.Logger;

public abstract class AbstractLoggerBehavior<T> extends AbstractBehavior<T> {

	public AbstractLoggerBehavior(ActorContext<T> context) {
		super(context);
		this.log = context.getLog();
		printActorSetup();
	}

	private void printActorSetup() {
		log.info("Actor {} setup with {}", this.getClass().getSimpleName(), this);
	}

	protected final Logger log;

}
