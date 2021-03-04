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

package com.openk9.ingestion.rabbitmq.service;

import com.openk9.ingestion.rabbitmq.wrapper.DeliveryWrapper;
import com.openk9.ingestion.api.Delivery;
import com.openk9.ingestion.api.Receiver;
import com.openk9.ingestion.api.ReceiverReactor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import reactor.core.publisher.Flux;
import reactor.rabbitmq.ConsumeOptions;

import java.util.function.Supplier;

@Component(
	immediate = true,
	service = {
		Receiver.class,
		ReceiverReactor.class
	}
)
public class ReceiverImpl implements ReceiverReactor {

	@Override
	public Flux<Delivery> consumeAutoAck(String queue) {
		return _senderProvider
			.get()
			.consumeAutoAck(queue)
			.map(DeliveryWrapper::new);
	}

	@Override
	public Flux<Delivery> consumeAutoAck(String queue, int prefetch) {
		return _senderProvider
			.get()
			.consumeAutoAck(queue, new ConsumeOptions().qos(prefetch))
			.map(DeliveryWrapper::new);
	}

	@Override
	public Flux<Delivery> consumeNoAck(String queue) {
		return _senderProvider
			.get()
			.consumeNoAck(queue)
			.map(DeliveryWrapper::new);
	}

	@Reference(target = "(rabbit=receiver)")
	private Supplier<reactor.rabbitmq.Receiver> _senderProvider;

}
