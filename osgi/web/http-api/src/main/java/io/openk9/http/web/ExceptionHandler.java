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

package io.openk9.http.web;

import io.netty.handler.codec.http.HttpResponseStatus;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerResponse;

public interface ExceptionHandler<T extends Throwable> {

	Class<T> getType();

	int getCode();

	default String getReason() {
		return null;
	}

	default Mono<Void> map(
		T throwable, HttpServerResponse response) {

		String reason = getReason();

		HttpResponseStatus httpResponseStatus;

		if (reason != null) {
			reason = reason.replaceAll("[\\n\\r]", "\t");
			httpResponseStatus = HttpResponseStatus.valueOf(getCode(), reason);
		}
		else {
			httpResponseStatus = HttpResponseStatus.valueOf(getCode());
		}

		response.status(httpResponseStatus);

		String message = throwable.getMessage();

		if (message != null && !message.isBlank()) {
			return response.sendString(Mono.just(message)).then();
		}

		return response.send();

	}

	class InternalServerErrorExceptionHandler
		implements ExceptionHandler<Throwable> {

		@Override
		public Class<Throwable> getType() {
			return Throwable.class;
		}

		@Override
		public int getCode() {
			return 500;
		}

	}

	ExceptionHandler<Throwable> INTERNAL_SERVER_ERROR =
		new InternalServerErrorExceptionHandler();

}
