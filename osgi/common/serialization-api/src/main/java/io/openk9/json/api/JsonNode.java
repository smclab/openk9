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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

public interface JsonNode extends Iterable<JsonNode> {

	JsonNode deepCopy();

	int size();

	boolean isEmpty();

	boolean isValueNode();

	boolean isContainerNode();

	boolean isMissingNode();

	boolean isArray();

	boolean isObject();

	JsonNode get(int index);

	JsonNode get(String fieldName);

	JsonNode path(String fieldName);

	JsonNode path(int index);

	Iterator<String> fieldNames();

	JsonNode at(JsonPointer ptr);

	JsonNode at(String jsonPtrExpr);

	JsonNodeType getNodeType();

	boolean isPojo();

	boolean isNumber();

	boolean isIntegralNumber();

	boolean isFloatingPointNumber();

	boolean isShort();

	boolean isInt();

	boolean isLong();

	boolean isFloat();

	boolean isDouble();

	boolean isBigDecimal();

	boolean isBigInteger();

	boolean isTextual();

	boolean isBoolean();

	boolean isNull();

	boolean isBinary();

	boolean canConvertToInt();

	boolean canConvertToLong();

	String textValue();

	byte[] binaryValue() throws java.io.IOException;

	boolean booleanValue();

	Number numberValue();

	short shortValue();

	int intValue();

	long longValue();

	float floatValue();

	double doubleValue();

	BigDecimal decimalValue();

	BigInteger bigIntegerValue();

	String asText();

	String asText(String defaultValue);

	int asInt();

	int asInt(int defaultValue);

	long asLong();

	long asLong(long defaultValue);

	double asDouble();

	double asDouble(double defaultValue);

	boolean asBoolean();

	boolean asBoolean(boolean defaultValue);

	JsonNode require() throws IllegalArgumentException;

	JsonNode requireNonNull() throws IllegalArgumentException;

	JsonNode required(String fieldName) throws IllegalArgumentException;

	JsonNode required(int index) throws IllegalArgumentException;

	JsonNode requiredAt(String pathExpr)
		throws IllegalArgumentException;

	JsonNode requiredAt(JsonPointer path)
			throws IllegalArgumentException;

	boolean has(String fieldName);

	boolean has(int index);

	boolean hasNonNull(String fieldName);

	boolean hasNonNull(int index);

	JsonNode findValue(String fieldName);

	List<? extends JsonNode> findValues(String fieldName);

	List<String> findValuesAsText(String fieldName);

	JsonNode findPath(String fieldName);

	JsonNode findParent(String fieldName);

	List<? extends JsonNode> findParents(String fieldName);

	List<? extends JsonNode> findValues(
		String fieldName, List<? super JsonNode> foundSoFar);

	List<String> findValuesAsText(
		String fieldName, List<String> foundSoFar);

	List<? extends JsonNode> findParents(
		String fieldName, List<? super JsonNode> foundSoFar);

	JsonNode with(String propertyName);

	JsonNode withArray(String propertyName);

	String toPrettyString();

	JsonToken asToken();

	JsonParser.NumberType numberType();

	JsonParser traverse();

	JsonParser traverse(ObjectCodec codec);

	void forEach(Consumer<? super JsonNode> action);

	boolean isEmpty(SerializerProvider serializers);

	void serialize(JsonGenerator gen, SerializerProvider serializers)
							throws java.io.IOException;

	void serializeWithType(
		JsonGenerator gen, SerializerProvider serializers,
		TypeSerializer typeSer) throws java.io.IOException;

	@Override
	boolean equals(Object jsonNode);

	ObjectNode toObjectNode();

	ArrayNode toArrayNode();

}
