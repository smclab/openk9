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

package io.openk9.sql.api.client;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

public class DatabaseClientUtil {

	public static <T> Mono<T> makeTransactional(
		ConnectionFactory connectionFactory, Mono<T> publisher) {
		return Mono.usingWhen(
			connectionFactory.create(),
			connection ->
				Mono.usingWhen(
					connection.beginTransaction(),
					unused -> publisher.contextWrite(Context.of(TRANSACTION_KEY, connection)),
					unused -> connection.commitTransaction(),
					(unused, t) -> {
						if (_log.isErrorEnabled()) {
							_log.error(t.getMessage(), t);
						}
						return connection.rollbackTransaction();
					},
					unused -> connection.rollbackTransaction()),
			Connection::close
		);
	}

	public static <T> Flux<T> makeTransactional(
		ConnectionFactory connectionFactory, Flux<T> publisher) {

		return Flux.usingWhen(
			connectionFactory.create(),
			connection ->
				Flux.usingWhen(
					connection.beginTransaction(),
					unused -> publisher.contextWrite(Context.of(TRANSACTION_KEY, connection)),
					unused -> connection.commitTransaction(),
					(unused, t) -> {
						if (_log.isErrorEnabled()) {
							_log.error(t.getMessage(), t);
						}
						return connection.rollbackTransaction();
					},
					unused -> connection.rollbackTransaction()),
			Connection::close
		);

	}

	public static final String TRANSACTION_KEY = "SESSION";

	private static final Logger _log = LoggerFactory.getLogger(
		DatabaseClientUtil.class);


}
