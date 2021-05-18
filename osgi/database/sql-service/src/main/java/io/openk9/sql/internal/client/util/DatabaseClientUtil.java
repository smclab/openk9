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

package io.openk9.sql.internal.client.util;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Statement;
import io.openk9.common.api.constant.Strings;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;

public final class DatabaseClientUtil {

	public static Statement bind(
		Map<String, Object> fieldValue, Statement statement) {

		int i = 1;

		for (Object value : fieldValue.values()) {
			statement = statement.bind(Strings.DOLLAR + i++, value);
		}

		return statement;

	}

	public static <T> Flux<T> safeConnection(
		ConnectionFactory connectionFactory,
		Function<Connection, Publisher<T>> executeQuery) {

		return Flux.usingWhen(
			connectionFactory.create(),
			executeQuery,
			Connection::close, (c, err) -> c.close(), Connection::close);
	}

}
