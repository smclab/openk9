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

import java.util.Date;
import java.util.Map;

public class BasicPropertiesWrapper
	implements BasicProperties, Delegate<AMQP.BasicProperties> {

	public BasicPropertiesWrapper(AMQP.BasicProperties beanProperties) {
		_delegate = beanProperties;
	}

	public String getContentType() {
		return this._delegate.getContentType();
	}

	public String getContentEncoding() {
		return this._delegate.getContentEncoding();
	}

	public Map<String, Object> getHeaders() {
		return this._delegate.getHeaders();
	}

	public Integer getDeliveryMode() {
		return this._delegate.getDeliveryMode();
	}

	public Integer getPriority() {
		return this._delegate.getPriority();
	}

	public String getCorrelationId() {
		return this._delegate.getCorrelationId();
	}

	public String getReplyTo() {
		return this._delegate.getReplyTo();
	}

	public String getExpiration() {
		return this._delegate.getExpiration();
	}

	public String getMessageId() {
		return this._delegate.getMessageId();
	}

	public Date getTimestamp() {
		return this._delegate.getTimestamp();
	}

	public String getType() {
		return this._delegate.getType();
	}

	public String getUserId() {
		return this._delegate.getUserId();
	}

	public String getAppId() {
		return this._delegate.getAppId();
	}

	public String getClusterId() {
		return this._delegate.getClusterId();
	}

	public AMQP.BasicProperties getDelegate() {
		return _delegate;
	}

	public static class BuilderWrapper implements Builder {

		public BuilderWrapper(AMQP.BasicProperties.Builder builder) {
			_delegate = builder;
		}

		public Builder contentType(String contentType) {
			this._delegate.contentType(contentType);
			return this;
		}

		public Builder contentEncoding(
			String contentEncoding) {
			this._delegate.contentEncoding(contentEncoding);
			return this;
		}

		public Builder headers(Map<String, Object> headers) {
			this._delegate.headers(headers);
			return this;
		}

		public Builder deliveryMode(Integer deliveryMode) {
			this._delegate.deliveryMode(deliveryMode);
			return this;
		}

		public Builder priority(Integer priority) {
			this._delegate.priority(priority);
			return this;
		}

		public Builder correlationId(String correlationId) {
			this._delegate.correlationId(correlationId);
			return this;
		}

		public Builder replyTo(String replyTo) {
			this._delegate.replyTo(replyTo);
			return this;
		}

		public Builder expiration(String expiration) {
			this._delegate.expiration(expiration);
			return this;
		}

		public Builder messageId(String messageId) {
			this._delegate.messageId(messageId);
			return this;
		}

		public Builder timestamp(Date timestamp) {
			this._delegate.timestamp(timestamp);
			return this;
		}

		public Builder type(String type) {
			this._delegate.type(type);
			return this;
		}

		public Builder userId(String userId) {
			this._delegate.userId(userId);
			return this;
		}

		public Builder appId(String appId) {
			this._delegate.appId(appId);
			return this;
		}

		public Builder clusterId(String clusterId) {
			this._delegate.clusterId(clusterId);
			return this;
		}

		public BasicProperties build() {
			return new BasicPropertiesWrapper(this._delegate.build());
		}

		private final AMQP.BasicProperties.Builder _delegate;
	}

	private final AMQP.BasicProperties _delegate;

}
