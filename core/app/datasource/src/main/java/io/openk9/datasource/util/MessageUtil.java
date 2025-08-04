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

package io.openk9.datasource.util;

import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Message;

public class MessageUtil {

	public static JsonObject toJsonObject(Message<?> message) {

		Object payload = message.getPayload();

		if (payload instanceof JsonObject) {
			return (JsonObject) payload;
		}

		if (payload instanceof String) {
			return new JsonObject((String) payload);
		}

		if (payload instanceof byte[]) {
			return new JsonObject(new String((byte[]) payload));
		}

		throw new IllegalArgumentException("payload not supported");

	}

	public static <T> T toObj(Message<?> message, Class<T> clazz) {

		Object payload = message.getPayload();

		if (clazz.isInstance(payload)) {
			return clazz.cast(payload);
		}

		return toJsonObject(message).mapTo(clazz);

	}

}
