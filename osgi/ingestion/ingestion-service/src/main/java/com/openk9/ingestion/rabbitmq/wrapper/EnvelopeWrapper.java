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

package com.openk9.ingestion.rabbitmq.wrapper;

import com.openk9.ingestion.api.Envelope;

public class EnvelopeWrapper
	implements Envelope, Delegate<com.rabbitmq.client.Envelope> {

	public EnvelopeWrapper(com.rabbitmq.client.Envelope envelope) {
		_delegate = envelope;
	}

	public long getDeliveryTag() {
		return this._delegate.getDeliveryTag();
	}

	public boolean isRedeliver() {
		return this._delegate.isRedeliver();
	}

	public String getExchange() {
		return this._delegate.getExchange();
	}

	public String getRoutingKey() {
		return this._delegate.getRoutingKey();
	}

	public com.rabbitmq.client.Envelope getDelegate() {
		return _delegate;
	}

	private final com.rabbitmq.client.Envelope _delegate;

}
