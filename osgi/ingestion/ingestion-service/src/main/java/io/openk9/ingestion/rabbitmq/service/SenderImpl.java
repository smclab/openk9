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

package io.openk9.ingestion.rabbitmq.service;

import io.openk9.ingestion.api.OutboundMessage;
import io.openk9.ingestion.api.Sender;
import io.openk9.ingestion.api.SenderReactor;
import io.openk9.ingestion.rabbitmq.wrapper.OutboundMessageWrapper;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

@Component(
	immediate = true,
	service = {
		Sender.class,
		SenderReactor.class
	}
)
public class SenderImpl implements SenderReactor {

	@Override
	public Publisher<Void> send(
		Publisher<OutboundMessage> publisher) {
		return send(Flux.from(publisher));
	}

	@Override
	public Mono<Void> sendMono(Mono<OutboundMessage> publisher) {
		return _senderProvider.get().send(
			publisher
				.cast(OutboundMessageWrapper.class)
				.map(OutboundMessageWrapper::getDelegate));
	}

	@Override
	public Mono<Void> send(
		Flux<OutboundMessage> publisher) {

		return _senderProvider.get().send(
			publisher
				.cast(OutboundMessageWrapper.class)
				.map(OutboundMessageWrapper::getDelegate));

	}



	@Reference(target = "(rabbit=sender)")
	private Supplier<reactor.rabbitmq.Sender> _senderProvider;


}
