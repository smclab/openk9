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

package com.openk9.ingestion.api;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

public interface Binding {

	String RABBIT_EXCHANGE = "Rabbit-Exchange";
	String RABBIT_ROUTING_KEY = "Rabbit-Routing-Key";
	String RABBIT_QUEUE = "Rabbit-Queue";

	Exchange getExchange();

	String getRoutingKey();

	String getQueue();

	static Binding of(
		Exchange exchange, String routingKey, String queue) {

		return new Default(exchange, routingKey, queue);

	}

	static Binding of(
		String exchangeName, Exchange.Type exchangeType, String routingKey,
		String queue) {

		return new Default(
			Exchange.of(exchangeName, exchangeType), routingKey, queue);

	}

	static Default.DefaultBuilder builder() {
		return Default.builder();
	}

	@Data
	@RequiredArgsConstructor(staticName = "of")
	@Builder
	class Exchange {
		private final String name;
		private final Type type;

		public enum Type {
			direct, topic, headers, fanout
		}

	}

	@Data
	@RequiredArgsConstructor(staticName = "of")
	@Builder
	class Default implements Binding {
		private final Exchange exchange;
		private final String routingKey;
		private final String queue;
	}

}
