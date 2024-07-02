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

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.spi.json.JsonProvider;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class VertxJsonNodeJsonProvider implements JsonProvider {

	public static final VertxJsonNodeJsonProvider INSTANCE = new VertxJsonNodeJsonProvider();
	public static final Configuration CONFIGURATION = Configuration.builder()
		.jsonProvider(INSTANCE)
		.build();

	private VertxJsonNodeJsonProvider() {}

	@Override
	public Object parse(String json) throws InvalidJsonException {
		return Json.decodeValue(json);
	}

	@Override
	public Object parse(InputStream jsonStream, String charset)
		throws InvalidJsonException {
		try {
			return new JsonObject(Buffer.buffer(jsonStream.readAllBytes()));
		}
		catch (IOException e) {
			throw new InvalidJsonException(e);
		}
	}

	@Override
	public String toJson(Object obj) {
		return obj.toString();
	}

	@Override
	public Object createArray() {
		return new JsonArray();
	}

	@Override
	public Object createMap() {
		return new JsonObject();
	}

	@Override
	public boolean isArray(Object obj) {
		return obj instanceof JsonArray;
	}

	@Override
	public int length(Object obj) {
		return isArray(obj)
			? ((JsonArray) obj).size()
			: isMap(obj)
				? ((JsonObject) obj).size()
				: 0;
	}

	@Override
	public Iterable<?> toIterable(Object obj) {
		return obj instanceof Iterable<?> ? (Iterable<?>) obj : null;
	}

	@Override
	public Collection<String> getPropertyKeys(Object obj) {
		if (isMap(obj)) {
			return ((JsonObject) obj).fieldNames();
		}
		return null;
	}

	@Override
	public Object getArrayIndex(Object obj, int idx) {
		if (isArray(obj)) {
			return ((JsonArray) obj).getValue(idx);
		}
		return null;
	}

	@Override
	public Object getArrayIndex(Object obj, int idx, boolean unwrap) {
		if (isArray(obj)) {
			return ((JsonArray) obj).getValue(idx);
		}
		return null;
	}

	@Override
	public void setArrayIndex(Object array, int idx, Object newValue) {
		if (isArray(array)) {
			JsonArray jsonArray = (JsonArray) array;
			if (idx == jsonArray.size()) {
				jsonArray.add(newValue);
			}
			else {
				jsonArray.set(idx, newValue);
			}
		}
	}

	@Override
	public Object getMapValue(Object obj, String key) {
		if (isMap(obj)) {
			return ((JsonObject) obj).getValue(key);
		}
		return null;
	}

	@Override
	public void setProperty(Object obj, Object key, Object value) {
		if (isMap(obj)) {
			((JsonObject) obj).put((String) key, value);
		}
	}

	@Override
	public void removeProperty(Object obj, Object key) {
		if (isMap(obj)) {
			((JsonObject) obj).remove((String) key);
		}
	}

	@Override
	public boolean isMap(Object obj) {
		return obj instanceof JsonObject;
	}

	@Override
	public Object unwrap(Object obj) {
		return obj;
	}

}
