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

import io.openk9.json.api.JsonNode;
import io.openk9.json.api.ObjectNode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ObjectNodeWrapper extends JsonNodeWrapper implements ObjectNode {

	public ObjectNodeWrapper(
		com.fasterxml.jackson.databind.node.ObjectNode delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	@Override
	public ObjectNode deepCopy() {
		return new ObjectNodeWrapper(this.delegate.deepCopy());
	}

	@Override
	public ObjectNode set(String fieldName, JsonNode value) {
		return new ObjectNodeWrapper(
			this.delegate.set(
				fieldName, ((JsonNodeWrapper)value).getDelegate()));
	}

	@Override
	public ObjectNode setAll(Map<String, ? extends JsonNode> properties) {
		return new ObjectNodeWrapper(
			this.delegate.setAll(fromWrapperToDelegate(properties)));
	}

	@Override
	public ObjectNode setAll(ObjectNode other) {
		return new ObjectNodeWrapper(
			this.delegate.setAll(((ObjectNodeWrapper)other).delegate));
	}

	@Override
	public JsonNode replace(String fieldName, JsonNode value) {
		return new JsonNodeWrapper(
			this.delegate.replace(
				fieldName, ((JsonNodeWrapper)value).getDelegate()));
	}

	@Override
	public ObjectNode without(String fieldName) {
		return new ObjectNodeWrapper(this.delegate.without(fieldName));
	}

	@Override
	public ObjectNode without(Collection<String> fieldNames) {
		return new ObjectNodeWrapper(this.delegate.without(fieldNames));
	}

	@Override
	public JsonNode put(String fieldName, JsonNode value) {
		return new JsonNodeWrapper(this.delegate.put(
			fieldName, ((JsonNodeWrapper)value).getDelegate()));
	}

	@Override
	public JsonNode remove(String fieldName) {

		com.fasterxml.jackson.databind.JsonNode jsonNode =
			this.delegate.remove(fieldName);

		if (jsonNode == null) {
			return null;
		}

		return new JsonNodeWrapper(jsonNode);
	}

	@Override
	public ObjectNode remove(Collection<String> fieldNames) {
		return new ObjectNodeWrapper(this.delegate.remove(fieldNames));
	}

	@Override
	public ObjectNode removeAll() {
		return new ObjectNodeWrapper(this.delegate.removeAll());
	}

	@Override
	public JsonNode putAll(Map<String, ? extends JsonNode> properties) {
		return new JsonNodeWrapper(
			this.delegate.putAll(fromWrapperToDelegate(properties)));
	}

	@Override
	public JsonNode putAll(ObjectNode other) {
		return new JsonNodeWrapper(
			this.delegate.putAll(((ObjectNodeWrapper)other).delegate));
	}

	@Override
	public ObjectNode retain(Collection<String> fieldNames) {
		return new ObjectNodeWrapper(this.delegate.retain(fieldNames));
	}

	@Override
	public ObjectNode retain(String... fieldNames) {
		return new ObjectNodeWrapper(this.delegate.retain(fieldNames));
	}

	@Override
	public ObjectNode putObject(String fieldName) {
		return new ObjectNodeWrapper(this.delegate.putObject(fieldName));
	}

	@Override
	public ObjectNode putPOJO(String fieldName, Object pojo) {
		return new ObjectNodeWrapper(this.delegate.putPOJO(fieldName, pojo));
	}

	@Override
	public ObjectNode putNull(String fieldName) {
		return new ObjectNodeWrapper(this.delegate.putNull(fieldName));
	}

	@Override
	public ObjectNode put(String fieldName, short v) {
		return new ObjectNodeWrapper(this.delegate.put(fieldName, v));
	}

	@Override
	public ObjectNode put(String fieldName, Short v) {
		return new ObjectNodeWrapper(this.delegate.put(fieldName, v));
	}

	@Override
	public ObjectNode put(String fieldName, int v) {
		return new ObjectNodeWrapper(this.delegate.put(fieldName, v));
	}

	@Override
	public ObjectNode put(String fieldName, Integer v) {
		return new ObjectNodeWrapper(this.delegate.put(fieldName, v));
	}

	@Override
	public ObjectNode put(String fieldName, long v) {
		return new ObjectNodeWrapper(this.delegate.put(fieldName, v));
	}

	@Override
	public ObjectNode put(String fieldName, Long v) {
		return new ObjectNodeWrapper(this.delegate.put(fieldName, v));
	}

	@Override
	public ObjectNode put(String fieldName, float v) {
		return new ObjectNodeWrapper(this.delegate.put(fieldName, v));
	}

	@Override
	public ObjectNode put(String fieldName, Float v) {
		return new ObjectNodeWrapper(this.delegate.put(fieldName, v));
	}

	@Override
	public ObjectNode put(String fieldName, double v) {
		return new ObjectNodeWrapper(this.delegate.put(fieldName, v));
	}

	@Override
	public ObjectNode put(String fieldName, Double v) {
		return new ObjectNodeWrapper(this.delegate.put(fieldName, v));
	}

	@Override
	public ObjectNode put(String fieldName, BigDecimal v) {
		return new ObjectNodeWrapper(this.delegate.put(fieldName, v));
	}

	@Override
	public ObjectNode put(String fieldName, BigInteger v) {
		return new ObjectNodeWrapper(this.delegate.put(fieldName, v));
	}

	@Override
	public ObjectNode put(String fieldName, String v) {
		return new ObjectNodeWrapper(this.delegate.put(fieldName, v));
	}

	@Override
	public ObjectNode put(String fieldName, boolean v) {
		return new ObjectNodeWrapper(this.delegate.put(fieldName, v));
	}

	@Override
	public ObjectNode put(String fieldName, Boolean v) {
		return new ObjectNodeWrapper(this.delegate.put(fieldName, v));
	}

	@Override
	public ObjectNode put(String fieldName, byte[] v) {
		return new ObjectNodeWrapper(this.delegate.put(fieldName, v));
	}

	@Override
	public Iterator<JsonNode> elements() {
		return super.iterator();
	}

	@Override
	public Set<Map.Entry<String, JsonNode>> fields() {

		HashSet<Map.Entry<String, JsonNode>> objects = new HashSet<>();

		Iterator<Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>>
			fields = delegate.fields();

		while (fields.hasNext()) {
			Map.Entry<String, com.fasterxml.jackson.databind.JsonNode> next =
				fields.next();
			objects.add(
				new AbstractMap.SimpleImmutableEntry<>(
					next.getKey(), new JsonNodeWrapper(next.getValue())));

		}

		return objects;
	}

	@Override
	public Stream<Map.Entry<String, JsonNode>> stream() {

		Iterator<Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>>
			fields = delegate.fields();

		Stream<Map.Entry<String, JsonNode>> stream = StreamSupport.stream(
			Spliterators.spliteratorUnknownSize(fields, Spliterator.ORDERED),
			false)
			.map(entry -> new AbstractMap.SimpleImmutableEntry<>(
				entry.getKey(), new JsonNodeWrapper(entry.getValue())));

		return stream;
	}

	@Override
	public ObjectNode toObjectNode() {
		return this;
	}

	@Override
	public Map<String, Object> toMap() {

		Iterator<Map.Entry<String, com.fasterxml.jackson.databind.JsonNode>>
			fields = delegate.fields();

		Map<String, Object> result = new HashMap<>();

		while (fields.hasNext()) {
			Map.Entry<String, com.fasterxml.jackson.databind.JsonNode> entry =
				fields.next();
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}

	@Override
	public ObjectNode merge(ObjectNode objectNode) {

		com.fasterxml.jackson.databind.JsonNode jsonNode =
			_merge(this.delegate, ((ObjectNodeWrapper)objectNode).delegate);

		return new ObjectNodeWrapper(
			(com.fasterxml.jackson.databind.node.ObjectNode)jsonNode);
	}

	public static com.fasterxml.jackson.databind.JsonNode _merge(
		com.fasterxml.jackson.databind.JsonNode mainNode,
		com.fasterxml.jackson.databind.JsonNode updateNode) {

		Iterator<String> fieldNames = updateNode.fieldNames();
		while (fieldNames.hasNext()) {

			String fieldName = fieldNames.next();
			com.fasterxml.jackson.databind.JsonNode jsonNode = mainNode.get(fieldName);
			// if field exists and is an embedded object
			if (jsonNode != null && jsonNode.isObject()) {
				_merge(jsonNode, updateNode.get(fieldName));
			}
			else {
				if (mainNode instanceof com.fasterxml.jackson.databind.node.ObjectNode) {
					// Overwrite field
					com.fasterxml.jackson.databind.JsonNode value = updateNode.get(fieldName);
					((com.fasterxml.jackson.databind.node.ObjectNode) mainNode).put(fieldName, value);
				}
			}

		}

		return mainNode;
	}

	private Map<String, ? extends com.fasterxml.jackson.databind.JsonNode>
		fromWrapperToDelegate(Map<String, ? extends JsonNode> properties) {

		return properties
			.entrySet()
			.stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				e -> ((JsonNodeWrapper)e.getValue()).getDelegate()));
	}

	private final com.fasterxml.jackson.databind.node.ObjectNode delegate;

}
