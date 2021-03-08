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

package io.openk9.cbor.api;

import com.fasterxml.jackson.core.type.TypeReference;
import io.openk9.json.api.JsonNode;
import io.openk9.json.api.ArrayNode;
import io.openk9.json.api.ObjectNode;

import java.util.List;
import java.util.Map;

public interface CBORFactory {

	byte[] toCBOR(Object o);

	<T> T fromCBOR(byte[] cbor, Class<T> clazz);

	<T> List<T> fromCBORList(byte[] cbor, Class<T> clazz);

	<T> T[] fromCBORArray(byte[] cbor, Class<T> clazz);

	<T> Map<String, T> fromCBORMap(byte[] cbor, Class<T> clazz);

	<T> T fromCBORMap(byte[] cbor, TypeReference<T> typeReference);

	JsonNode fromCBORToJsonNode(String cbor);

	JsonNode fromCBORToJsonNode(byte[] cbor);

	JsonNode treeNode(Object object);

	ObjectNode createObjectNode();

	ArrayNode createArrayNode();

}
