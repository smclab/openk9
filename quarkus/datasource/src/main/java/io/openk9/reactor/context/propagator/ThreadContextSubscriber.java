/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.reactor.context.propagator;

import org.eclipse.microprofile.context.ThreadContext;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.publisher.Operators;

import java.util.function.Function;

public class ThreadContextSubscriber<T> implements CoreSubscriber<T>, Fuseable.QueueSubscription<T> {
	private final Subscriber<? super T> subscriber;
	private final ThreadContext context;
	private Subscription subscription;

	public ThreadContextSubscriber(Subscriber<? super T> subscriber, ThreadContext ctx) {
		this.subscriber = subscriber;
		this.context = ctx;
	}


	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		withThreadContext(() -> subscriber.onSubscribe(this));
	}

	@Override
	public void request(long n) {
		withThreadContext(() -> subscription.request(n));
	}

	@Override
	public void onNext(T o) {
		withThreadContext(() -> subscriber.onNext(o));
	}

	@Override
	public void cancel() {
		withThreadContext(() -> subscription.cancel());
	}

	@Override
	public void onError(Throwable throwable) {
		withThreadContext(() -> subscriber.onError(throwable));
	}

	@Override
	public void onComplete() {
		withThreadContext(subscriber::onComplete);
	}

	private void withThreadContext(Runnable runnable) {
		context.contextualRunnable(runnable);
	}

	public static <T> Function<? super Publisher<T>, ? extends Publisher<T>> asOperator(ThreadContext context) {
		return Operators.liftPublisher((publisher, sub) -> {
			// if Flux/Mono #just, #empty, #error
			if (publisher instanceof Fuseable.ScalarCallable) {
				return sub;
			}

			return new ThreadContextSubscriber<>(sub, context);
		});
	}

	@Override
	public int requestFusion(int requestedMode) {
		return Fuseable.NONE; //always negotiate to no fusion
	}

	@Override
	public T poll() {
		return null;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public void clear() {

	}
}