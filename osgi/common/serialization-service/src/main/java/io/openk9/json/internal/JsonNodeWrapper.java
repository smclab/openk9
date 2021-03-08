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

package io.openk9.json.internal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import io.openk9.json.api.ArrayNode;
import io.openk9.json.api.JsonNode;
import io.openk9.json.api.ObjectNode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JsonNodeWrapper implements JsonNode {

	public JsonNodeWrapper(com.fasterxml.jackson.databind.JsonNode delegate) {
		this.delegate = delegate;
	}

	@Override
	public JsonNode deepCopy() {
		return new JsonNodeWrapper(this.delegate.deepCopy());
	}

	@Override
	public int size() {
		return this.delegate.size();
	}

	@Override
	public boolean isEmpty() {
		return this.delegate.isEmpty();
	}

	@Override
	public boolean isValueNode() {
		return this.delegate.isValueNode();
	}

	@Override
	public boolean isContainerNode() {
		return this.delegate.isContainerNode();
	}

	@Override
	public boolean isMissingNode() {
		return this.delegate.isMissingNode();
	}

	@Override
	public boolean isArray() {
		return this.delegate.isArray();
	}

	@Override
	public boolean isObject() {
		return this.delegate.isObject();
	}

	@Override
	public JsonNode get(int index) {

		com.fasterxml.jackson.databind.JsonNode jsonNode =
			this.delegate.get(index);

		if (jsonNode == null) {
			return null;
		}


		return new JsonNodeWrapper(jsonNode);
	}

	@Override
	public JsonNode get(String fieldName) {

		com.fasterxml.jackson.databind.JsonNode jsonNode =
			this.delegate.get(fieldName);

		if (jsonNode == null) {
			return null;
		}

		return new JsonNodeWrapper(jsonNode);
	}

	@Override
	public JsonNode path(String fieldName) {
		return new JsonNodeWrapper(this.delegate.path(fieldName));
	}

	@Override
	public JsonNode path(int index) {
		return new JsonNodeWrapper(this.delegate.path(index));
	}

	@Override
	public Iterator<String> fieldNames() {
		return this.delegate.fieldNames();
	}

	@Override
	public JsonNode at(JsonPointer ptr) {
		return new JsonNodeWrapper(this.delegate.at(ptr));
	}

	@Override
	public JsonNode at(String jsonPtrExpr) {
		return new JsonNodeWrapper(this.delegate.at(jsonPtrExpr));
	}

	@Override
	public JsonNodeType getNodeType() {
		return this.delegate.getNodeType();
	}

	@Override
	public boolean isPojo() {
		return this.delegate.isPojo();
	}

	@Override
	public boolean isNumber() {
		return this.delegate.isNumber();
	}

	@Override
	public boolean isIntegralNumber() {
		return this.delegate.isIntegralNumber();
	}

	@Override
	public boolean isFloatingPointNumber() {
		return this.delegate.isFloatingPointNumber();
	}

	@Override
	public boolean isShort() {
		return this.delegate.isShort();
	}

	@Override
	public boolean isInt() {
		return this.delegate.isInt();
	}

	@Override
	public boolean isLong() {
		return this.delegate.isLong();
	}

	@Override
	public boolean isFloat() {
		return this.delegate.isFloat();
	}

	@Override
	public boolean isDouble() {
		return this.delegate.isDouble();
	}

	@Override
	public boolean isBigDecimal() {
		return this.delegate.isBigDecimal();
	}

	@Override
	public boolean isBigInteger() {
		return this.delegate.isBigInteger();
	}

	@Override
	public boolean isTextual() {
		return this.delegate.isTextual();
	}

	@Override
	public boolean isBoolean() {
		return this.delegate.isBoolean();
	}

	@Override
	public boolean isNull() {
		return this.delegate.isNull();
	}

	@Override
	public boolean isBinary() {
		return this.delegate.isBinary();
	}

	@Override
	public boolean canConvertToInt() {
		return this.delegate.canConvertToInt();
	}

	@Override
	public boolean canConvertToLong() {
		return this.delegate.canConvertToLong();
	}

	@Override
	public String textValue() {
		return this.delegate.textValue();
	}

	@Override
	public byte[] binaryValue() throws java.io.IOException {
		return this.delegate.binaryValue();
	}

	@Override
	public boolean booleanValue() {
		return this.delegate.booleanValue();
	}

	@Override
	public Number numberValue() {
		return this.delegate.numberValue();
	}

	@Override
	public short shortValue() {
		return this.delegate.shortValue();
	}

	@Override
	public int intValue() {
		return this.delegate.intValue();
	}

	@Override
	public long longValue() {
		return this.delegate.longValue();
	}

	@Override
	public float floatValue() {
		return this.delegate.floatValue();
	}

	@Override
	public double doubleValue() {
		return this.delegate.doubleValue();
	}

	@Override
	public BigDecimal decimalValue() {
		return this.delegate.decimalValue();
	}

	@Override
	public BigInteger bigIntegerValue() {
		return this.delegate.bigIntegerValue();
	}

	@Override
	public String asText() {
		return this.delegate.asText();
	}

	@Override
	public String asText(String defaultValue) {
		return this.delegate.asText(defaultValue);
	}

	@Override
	public int asInt() {
		return this.delegate.asInt();
	}

	@Override
	public int asInt(int defaultValue) {
		return this.delegate.asInt(defaultValue);
	}

	@Override
	public long asLong() {
		return this.delegate.asLong();
	}

	@Override
	public long asLong(long defaultValue) {
		return this.delegate.asLong(defaultValue);
	}

	@Override
	public double asDouble() {
		return this.delegate.asDouble();
	}

	@Override
	public double asDouble(double defaultValue) {
		return this.delegate.asDouble(defaultValue);
	}

	@Override
	public boolean asBoolean() {
		return this.delegate.asBoolean();
	}

	@Override
	public boolean asBoolean(boolean defaultValue) {
		return this.delegate.asBoolean(defaultValue);
	}

	@Override
	public JsonNode require() throws IllegalArgumentException {
		return new JsonNodeWrapper(this.delegate.require());
	}

	@Override
	public JsonNode requireNonNull() throws IllegalArgumentException {
		return new JsonNodeWrapper(this.delegate.requireNonNull());
	}

	@Override
	public JsonNode required(String fieldName) throws IllegalArgumentException {
		return new JsonNodeWrapper(this.delegate.required(fieldName));
	}

	@Override
	public JsonNode required(int index) throws IllegalArgumentException {
		return new JsonNodeWrapper(this.delegate.required(index));
	}

	@Override
	public JsonNode requiredAt(String pathExpr)
		throws IllegalArgumentException {
		return new JsonNodeWrapper(this.delegate.requiredAt(pathExpr));
	}

	@Override
	public JsonNode requiredAt(JsonPointer path)
		throws IllegalArgumentException {
		return new JsonNodeWrapper(this.delegate.requiredAt(path));
	}

	@Override
	public boolean has(String fieldName) {
		return this.delegate.has(fieldName);
	}

	@Override
	public boolean has(int index) {
		return this.delegate.has(index);
	}

	@Override
	public boolean hasNonNull(String fieldName) {
		return this.delegate.hasNonNull(fieldName);
	}

	@Override
	public boolean hasNonNull(int index) {
		return this.delegate.hasNonNull(index);
	}

	@Override
	public JsonNode findValue(String fieldName) {
		return new JsonNodeWrapper(this.delegate.findValue(fieldName));
	}

	@Override
	public List<JsonNode> findValues(String fieldName) {
		return fromDelegateToWrapper(this.delegate.findValues(fieldName));
	}

	@Override
	public List<String> findValuesAsText(String fieldName) {
		return this.delegate.findValuesAsText(fieldName);
	}

	@Override
	public JsonNode findPath(String fieldName) {
		return new JsonNodeWrapper(this.delegate.findPath(fieldName));
	}

	@Override
	public JsonNode findParent(String fieldName) {
		return new JsonNodeWrapper(this.delegate.findParent(fieldName));
	}

	@Override
	public List<JsonNode> findParents(String fieldName) {
		return fromDelegateToWrapper(this.delegate.findParents(fieldName));
	}

	@Override
	public List<String> findValuesAsText(
		String fieldName, List<String> foundSoFar) {
		return this.delegate.findValuesAsText(fieldName, foundSoFar);
	}


	@Override
	public JsonNode with(String propertyName) {
		return new JsonNodeWrapper(this.delegate.with(propertyName));
	}

	@Override
	public JsonNode withArray(String propertyName) {
		return new JsonNodeWrapper(this.delegate.withArray(propertyName));
	}

	@Override
	public String toPrettyString() {
		return this.delegate.toPrettyString();
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	@Override
	public JsonToken asToken() {
		return this.delegate.asToken();
	}

	@Override
	public JsonParser.NumberType numberType() {
		return this.delegate.numberType();
	}

	@Override
	public JsonParser traverse() {
		return this.delegate.traverse();
	}

	@Override
	public JsonParser traverse(ObjectCodec codec) {
		return this.delegate.traverse(codec);
	}

	@Override
	public Iterator<JsonNode> iterator() {
		return StreamSupport
			.stream(spliterator(), false)
			.collect(Collectors.toList())
			.iterator();
	}

	@Override
	public Spliterator<JsonNode> spliterator() {
		return StreamSupport
			.stream(this.delegate.spliterator(), false)
			.map(JsonNodeWrapper::new)
			.map(e -> (JsonNode)e)
			.spliterator();
	}

	@Override
	public void forEach(Consumer<? super JsonNode> action) {
		this.iterator().forEachRemaining(action::accept);
	}

	@Override
	public boolean isEmpty(SerializerProvider serializers) {
		return this.delegate.isEmpty(serializers);
	}

	@Override
	public void serialize(JsonGenerator gen, SerializerProvider serializers)
		throws java.io.IOException {
		this.delegate.serialize(gen, serializers);
	}

	@Override
	public void serializeWithType(
		JsonGenerator gen, SerializerProvider serializers,
		TypeSerializer typeSer) throws java.io.IOException {
		this.delegate.serializeWithType(gen, serializers, typeSer);
	}

	@Override
	public boolean equals(Object jsonNode) {

		if (!(jsonNode instanceof JsonNode)) {
			return false;
		}

		return this.delegate.equals(((JsonNodeWrapper)jsonNode).delegate);
	}

	@Override
	public ObjectNode toObjectNode() {
		return new ObjectNodeWrapper(
			(com.fasterxml.jackson.databind.node.ObjectNode)this.delegate);
	}

	@Override
	public ArrayNode toArrayNode() {
		return new ArrayNodeWrapper(
			(com.fasterxml.jackson.databind.node.ArrayNode)this.delegate);
	}

	@Override
	public List<JsonNode> findValues(
		String fieldName, List<? super JsonNode> foundSoFar) {
		return fromDelegateToWrapper(
			this.delegate.findValues(
				fieldName, fromWrapperToDelegate(foundSoFar)));
	}

	@Override
	public List<JsonNode> findParents(
		String fieldName, List<? super JsonNode> foundSoFar) {
		return fromDelegateToWrapper(
			this.delegate.findParents(
				fieldName, fromWrapperToDelegate(foundSoFar)));
	}

	private List<JsonNode> fromDelegateToWrapper(
		List<com.fasterxml.jackson.databind.JsonNode> jsonNodes) {

		return jsonNodes
			.stream()
			.map(JsonNodeWrapper::new)
			.collect(Collectors.toList());

	}

	private List<com.fasterxml.jackson.databind.JsonNode> fromWrapperToDelegate(
		List<? super JsonNode> jsonNodes) {

		return jsonNodes
			.stream()
			.map(e -> ((JsonNodeWrapper)e).getDelegate())
			.collect(Collectors.toList());

	}

	protected com.fasterxml.jackson.databind.JsonNode getDelegate() {
		return delegate;
	}

	private final com.fasterxml.jackson.databind.JsonNode delegate;

}
