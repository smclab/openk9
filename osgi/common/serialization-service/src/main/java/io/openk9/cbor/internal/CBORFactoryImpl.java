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

package io.openk9.cbor.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.openk9.cbor.api.CBORFactory;
import io.openk9.json.api.ArrayNode;
import io.openk9.json.api.JsonNode;
import io.openk9.json.api.ObjectNode;
import io.openk9.json.internal.JsonNodeWrapper;
import io.vavr.CheckedFunction1;
import io.openk9.json.internal.ArrayNodeWrapper;
import io.openk9.json.internal.ObjectNodeWrapper;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.List;
import java.util.Map;

@Component(
	immediate = true,
	service = CBORFactory.class
)
public class CBORFactoryImpl implements CBORFactory {

	@Override
	public byte[] toCBOR(Object o) {
		return _exec(objectMapper -> objectMapper.writeValueAsBytes(o));
	}

	@Override
	public <T> T fromCBOR(byte[] cbor, Class<T> clazz) {
		return _exec(objectMapper -> objectMapper.readValue(cbor, clazz));
	}

	@Override
	public <T> List<T> fromCBORList(byte[] cbor, Class<T> clazz) {
		return _exec(
			objectMapper ->
				objectMapper.readerForListOf(clazz).readValue(cbor));
	}

	@Override
	public <T> T[] fromCBORArray(byte[] cbor, Class<T> clazz) {
		return _exec(
			objectMapper ->
				objectMapper.readerForArrayOf(clazz).readValue(cbor));
	}

	@Override
	public <T> Map<String, T> fromCBORMap(byte[] cbor, Class<T> clazz) {
		return _exec(
			objectMapper ->
				objectMapper.readerForMapOf(clazz).readValue(cbor));
	}

	private <T> T _exec(CheckedFunction1<ObjectMapper, T> function) {
		return function
			.unchecked()
			.apply(_objectMapperProvider.getObjectMapper());

	}

	@Override
	public JsonNode fromCBORToJsonNode(String cbor) {
		return _exec(objectMapper ->
			new JsonNodeWrapper(objectMapper.readTree(cbor)));
	}

	@Override
	public JsonNode fromCBORToJsonNode(byte[] cbor) {
		return _exec(objectMapper ->
			new JsonNodeWrapper(objectMapper.readTree(cbor)));
	}

	@Override
	public JsonNode treeNode(Object object) {
		return _exec(objectMapper ->
			new JsonNodeWrapper(objectMapper.valueToTree(object)));
	}

	@Override
	public ObjectNode createObjectNode() {
		return new ObjectNodeWrapper(
			_objectMapperProvider.getObjectMapper().createObjectNode());
	}

	@Override
	public ArrayNode createArrayNode() {
		return new ArrayNodeWrapper(
			_objectMapperProvider.getObjectMapper().createArrayNode());
	}

	@Reference
	private ObjectMapperProvider _objectMapperProvider;

}
