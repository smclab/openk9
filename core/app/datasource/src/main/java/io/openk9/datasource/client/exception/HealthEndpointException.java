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

package io.openk9.datasource.client.exception;

/**
 * Thrown when the {@link io.vertx.ext.web.client.WebClient} fails to
 * perform the HTTP call to a service's {@code /health} endpoint due
 * to a network-level error (e.g. connection refused, timeout, host unreachable).
 */
public class HealthEndpointException extends RuntimeException {

	/** Creates an exception with the given detail message. */
	public HealthEndpointException(String message) {
		super(message);
	}

	/** Creates an exception with the given detail message and cause. */
	public HealthEndpointException(String message, Throwable cause) {
		super(message, cause);
	}

	/** Creates an exception wrapping the given cause. */
	public HealthEndpointException(Throwable cause) {
		super(cause);
	}
}
