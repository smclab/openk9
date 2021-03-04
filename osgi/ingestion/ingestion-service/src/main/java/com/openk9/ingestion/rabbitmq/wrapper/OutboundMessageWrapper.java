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

import com.rabbitmq.client.AMQP;
import com.openk9.ingestion.api.BasicProperties;
import com.openk9.ingestion.api.OutboundMessage;

public class OutboundMessageWrapper
	implements OutboundMessage, Delegate<reactor.rabbitmq.OutboundMessage> {

	public OutboundMessageWrapper(reactor.rabbitmq.OutboundMessage _delegate) {
		this._delegate = _delegate;
	}

	public String getExchange() {
		return this._delegate.getExchange();
	}

	public String getRoutingKey() {
		return this._delegate.getRoutingKey();
	}

	public BasicProperties getProperties() {
		return new BasicPropertiesWrapper(this._delegate.getProperties());
	}

	public byte[] getBody() {
		return this._delegate.getBody();
	}

	@Override
	public reactor.rabbitmq.OutboundMessage getDelegate() {
		return _delegate;
	}

	public static class BuilderWrapper implements Builder {

		@Override
		public Builder exchange(String exchange) {
			_exchange = exchange;
			return this;
		}

		@Override
		public Builder routingKey(String routingKey) {
			_routingKey = routingKey;
			return this;
		}

		@Override
		public Builder properties(
			BasicProperties basicProperties) {
			_basicProperties = basicProperties;
			return this;
		}

		@Override
		public Builder body(byte[] body) {
			_body = body;
			return this;
		}

		@Override
		public OutboundMessage build() {

			AMQP.BasicProperties basicProperties = null;

			if (_basicProperties != null) {
				if (_basicProperties instanceof BasicPropertiesWrapper) {
					basicProperties =
						((BasicPropertiesWrapper) _basicProperties)
							.getDelegate();
				}
			}

			return new OutboundMessageWrapper(
				new reactor.rabbitmq.OutboundMessage(
					_exchange, _routingKey, basicProperties, _body
				)
			);

		}

		private String _exchange;
		private String _routingKey;
		private BasicProperties _basicProperties;
		private byte[] _body;

	}

	private final reactor.rabbitmq.OutboundMessage _delegate;
}
