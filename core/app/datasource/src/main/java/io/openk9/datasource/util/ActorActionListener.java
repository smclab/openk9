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

package io.openk9.datasource.util;

import akka.actor.typed.ActorRef;
import org.opensearch.action.ActionListener;

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
