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

package io.openk9.datasource.pipeline.actor.enrichitem;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;

import java.util.Base64;

public class TokenUtils {

	public static String encode(Token.SchedulingToken token) {
		String jsonToken = Json.encode(token);
		return Base64.getEncoder().encodeToString(jsonToken.getBytes());
	}

	public static Token.SchedulingToken decode(String value) {
		byte[] jsonToken = Base64.getDecoder().decode(value);
		return Json.decodeValue(Buffer.buffer(jsonToken), Token.SchedulingToken.class);
	}

}
