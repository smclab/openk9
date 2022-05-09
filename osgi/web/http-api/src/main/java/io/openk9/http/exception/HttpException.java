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

package io.openk9.http.exception;

import org.reactivestreams.Publisher;

public class HttpException extends RuntimeException {

	public HttpException(int statusCode, String reason) {
		super(reason);
		_reason = reason;
		_statusCode = statusCode;
	}

	public HttpException(
		int statusCode, String reason, Publisher<String> body) {
		this(statusCode, reason);
		_body = body;
	}

	public int getStatusCode() {
		return _statusCode;
	}

	public String getReason() {
		return _reason;
	}

	public Publisher<String> getBody() {
		return _body;
	}

	private final String _reason;

	private final int _statusCode;

	private Publisher<String> _body;

}
