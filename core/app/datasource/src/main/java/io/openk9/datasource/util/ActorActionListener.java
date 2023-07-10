package io.openk9.datasource.util;

import akka.actor.typed.ActorRef;
import org.elasticsearch.action.ActionListener;

import java.util.function.BiFunction;

public final class ActorActionListener<M, R> implements ActionListener<R> {

	private final ActorRef<M> replyTo;
	private final BiFunction<R, Throwable, M> messageFactory;

	private ActorActionListener(
		ActorRef<M> replyTo, BiFunction<R, Throwable, M> messageFactory) {
		this.replyTo = replyTo;
		this.messageFactory = messageFactory;
	}

	@Override
	public void onResponse(R response) {
		replyTo.tell(messageFactory.apply(response, null));
	}

	@Override
	public void onFailure(Exception e) {
		replyTo.tell(messageFactory.apply(null, e));
	}

	public static <M, R> ActionListener<R> of(
		ActorRef<M> replyTo, BiFunction<R, Throwable, M> messageFactory) {

		return new ActorActionListener<>(replyTo, messageFactory);
	}

}
