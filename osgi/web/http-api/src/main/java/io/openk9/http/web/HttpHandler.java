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

import org.reactivestreams.Publisher;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.util.function.BiFunction;

public interface HttpHandler
	extends BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>> {

	int GET =  		0b0_0_0_0_0_1;

	int POST = 		0b0_0_0_0_1_0;

	int PUT = 		0b0_0_0_1_0_0;

	int DELETE = 	0b0_0_1_0_0_0;

	int PATCH = 	0b0_1_0_0_0_0;

	int OPTIONS = 	0b1_0_0_0_0_0;

	int ALL = GET | POST | PUT | DELETE | PATCH | OPTIONS;

}
