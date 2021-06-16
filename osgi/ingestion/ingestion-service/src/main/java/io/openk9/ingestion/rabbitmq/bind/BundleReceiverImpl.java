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

package io.openk9.ingestion.rabbitmq.bind;

import io.openk9.ingestion.api.AcknowledgableDelivery;
import io.openk9.ingestion.api.BundleReceiver;
import io.openk9.ingestion.api.Delivery;
import io.openk9.ingestion.api.ReceiverReactor;
import reactor.core.publisher.Flux;

public class BundleReceiverImpl implements BundleReceiver {

	public BundleReceiverImpl(
		ReceiverReactor receiverReactor, String queue) {
		_receiverReactor = receiverReactor;
		_queue = queue;
	}

	@Override
	public Flux<Delivery> consumeAutoAck() {
		return _receiverReactor.consumeAutoAck(_queue);
	}

	@Override
	public Flux<Delivery> consumeAutoAck(int prefetch) {
		return _receiverReactor.consumeAutoAck(_queue, prefetch);
	}

	@Override
	public Flux<Delivery> consumeNoAck() {
		return _receiverReactor.consumeNoAck(_queue);
	}

	@Override
	public Flux<Delivery> consumeNoAck(int prefetch) {
		return _receiverReactor.consumeNoAck(_queue, prefetch);
	}

	@Override
	public Flux<AcknowledgableDelivery> consumeManualAck() {
		return _receiverReactor.consumeManualAck(_queue);
	}

	@Override
	public Flux<AcknowledgableDelivery> consumeManualAck(int prefetch) {
		return _receiverReactor.consumeManualAck(_queue, prefetch);
	}

	private final String _queue;
	private final ReceiverReactor _receiverReactor;

}
