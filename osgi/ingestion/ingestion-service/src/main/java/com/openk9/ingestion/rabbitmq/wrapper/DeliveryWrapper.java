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

import com.openk9.ingestion.api.BasicProperties;
import com.openk9.ingestion.api.Delivery;
import com.openk9.ingestion.api.Envelope;

public class DeliveryWrapper
	implements Delivery, Delegate<com.rabbitmq.client.Delivery> {

	public DeliveryWrapper(com.rabbitmq.client.Delivery _delegate) {
		this._delegate = _delegate;
	}

	public Envelope getEnvelope() {
		return new EnvelopeWrapper(this._delegate.getEnvelope());
	}

	public BasicProperties getProperties() {
		return new BasicPropertiesWrapper(this._delegate.getProperties());
	}

	public byte[] getBody() {
		return this._delegate.getBody();
	}

	public com.rabbitmq.client.Delivery getDelegate() {
		return _delegate;
	}

	private final com.rabbitmq.client.Delivery _delegate;

}
