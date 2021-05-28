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

package io.openk9.ingestion.api;

import reactor.core.publisher.Flux;

public interface ReceiverReactor extends Receiver {

	Flux<Delivery> consumeAutoAck(String queue);
	Flux<Delivery> consumeAutoAck(String queue, int prefetch);
	Flux<Delivery> consumeNoAck(String queue);
	Flux<Delivery> consumeNoAck(String queue, int prefetch);
	Flux<AcknowledgableDelivery> consumeManualAck(String queue);
	Flux<AcknowledgableDelivery> consumeManualAck(String queue, int prefetch);

}
