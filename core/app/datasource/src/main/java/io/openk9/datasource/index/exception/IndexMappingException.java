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

package io.openk9.datasource.index.exception;

public class IndexMappingException extends RuntimeException {

	/**
	 * The failure carries no HTTP status, because OpenSearch did not answer it.
	 */
	public static final int NO_STATUS = 0;

	private final int status;

	public IndexMappingException() {
		super();

		this.status = NO_STATUS;
	}

	public IndexMappingException(String message) {
		super(message);

		this.status = NO_STATUS;
	}

	/**
	 * @param message the explanation of OpenSearch, reported as is
	 * @param status the HTTP status OpenSearch answered with, so a caller can
	 * tell the cases apart without parsing the message
	 */
	public IndexMappingException(String message, int status) {
		super(message);

		this.status = status;
	}

	public IndexMappingException(String message, Throwable cause) {
		super(message, cause);

		this.status = NO_STATUS;
	}

	public IndexMappingException(Throwable cause) {
		super(cause);

		this.status = NO_STATUS;
	}

	protected IndexMappingException(
		String message,
		Throwable cause,
		boolean enableSuppression,
		boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);

		this.status = NO_STATUS;
	}

	public int getStatus() {
		return status;
	}
}
