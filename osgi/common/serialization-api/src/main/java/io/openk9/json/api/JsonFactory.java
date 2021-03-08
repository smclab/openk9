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

package io.openk9.json.api;

import java.util.List;
import java.util.Map;

public interface JsonFactory {

	String toJson(Object o);

	String toPrettyJson(Object o);

	<T> T fromJson(String json, Class<T> clazz);

	<T> List<T> fromJsonList(String jsonList, Class<T> clazz);

	<T> T[] fromJsonArray(String jsonList, Class<T> clazz);

	<T> Map<String, T> fromJsonMap(String jsonList, Class<T> clazz);

	JsonNode fromJsonToJsonNode(String json);

	JsonNode fromJsonToJsonNode(byte[] json);

	Object toJsonClassDefinition(Class<?> clazz);

	ObjectNode createObjectNode();

	ArrayNode createArrayNode();
}
