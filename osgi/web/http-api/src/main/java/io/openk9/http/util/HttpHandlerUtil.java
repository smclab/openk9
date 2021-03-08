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

package io.openk9.http.util;

import io.openk9.http.web.HttpHandler;

import java.util.ArrayList;
import java.util.List;

public class HttpHandlerUtil {

	public static List<String> bitsToMethodName(int httpMethods) {

		List<String> methodNames = new ArrayList<>();

		if ((httpMethods & HttpHandler.POST) != 0) {
			methodNames.add("POST");
		}
		else if ((httpMethods & HttpHandler.GET) != 0) {
			methodNames.add("GET");
		}
		else if ((httpMethods & HttpHandler.PUT) != 0) {
			methodNames.add("PUT");
		}
		else if ((httpMethods & HttpHandler.DELETE) != 0) {
			methodNames.add("DELETE");
		}
		else if ((httpMethods & HttpHandler.PATCH) != 0) {
			methodNames.add("PATCH");
		}
		else if ((httpMethods & HttpHandler.OPTIONS) != 0) {
			methodNames.add("OPTIONS");
		}

		return methodNames;

	}

}
