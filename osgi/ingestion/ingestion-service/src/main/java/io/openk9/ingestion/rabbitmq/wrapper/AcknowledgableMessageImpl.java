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

package io.openk9.ingestion.rabbitmq.wrapper;

import io.openk9.ingestion.api.AcknowledgableMessage;
import reactor.rabbitmq.AcknowledgableDelivery;

public class AcknowledgableMessageImpl<T>
	extends AcknowledgableDeliveryWrapper implements AcknowledgableMessage<T> {

	public AcknowledgableMessageImpl(
		AcknowledgableDelivery delegate, T message) {
		super(delegate);
		_message = message;
	}

	@Override
	public T getMessage() {
		return _message;
	}

	private T _message;

}
