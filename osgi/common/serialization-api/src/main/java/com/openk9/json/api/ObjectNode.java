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

package com.openk9.json.api;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public interface ObjectNode extends JsonNode {

	@Override
	ObjectNode deepCopy();

	ObjectNode set(String fieldName, JsonNode value);

	ObjectNode setAll(Map<String, ? extends JsonNode> properties);

	ObjectNode setAll(ObjectNode other);

	JsonNode replace(String fieldName, JsonNode value);

	ObjectNode without(String fieldName);

	ObjectNode without(Collection<String> fieldNames);

	JsonNode put(String fieldName, JsonNode value);

	JsonNode remove(String fieldName);

	ObjectNode remove(Collection<String> fieldNames);

	ObjectNode removeAll();

	JsonNode putAll(Map<String, ? extends JsonNode> properties);

	JsonNode putAll(ObjectNode other);

	ObjectNode retain(Collection<String> fieldNames);

	ObjectNode retain(String... fieldNames);

	ObjectNode putObject(String fieldName);

	ObjectNode putPOJO(String fieldName, Object pojo);

	ObjectNode putNull(String fieldName);

	ObjectNode put(String fieldName, short v);

	ObjectNode put(String fieldName, Short v);

	ObjectNode put(String fieldName, int v);

	ObjectNode put(String fieldName, Integer v);

	ObjectNode put(String fieldName, long v);

	ObjectNode put(String fieldName, Long v);

	ObjectNode put(String fieldName, float v);

	ObjectNode put(String fieldName, Float v);

	ObjectNode put(String fieldName, double v);

	ObjectNode put(String fieldName, Double v);

	ObjectNode put(String fieldName, BigDecimal v);

	ObjectNode put(String fieldName, BigInteger v);

	ObjectNode put(String fieldName, String v);

	ObjectNode put(String fieldName, boolean v);

	ObjectNode put(String fieldName, Boolean v);

	ObjectNode put(String fieldName, byte[] v);

	Iterator<JsonNode> elements();

	Set<Map.Entry<String, JsonNode>> fields();

	Stream<Map.Entry<String, JsonNode>> stream();

	Map<String, Object> toMap();

	ObjectNode merge(ObjectNode objectNode);
}
