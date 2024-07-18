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

package io.openk9.datasource;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.io.InputStream;

public class TestUtils {
	public static InputStream getResourceAsStream(String path) {
		return TestUtils.class
			.getClassLoader()
			.getResourceAsStream(path);
	}

	public static JsonObject getResourceAsJsonObject(String path) {
		try (InputStream in = getResourceAsStream(path)) {
			return (JsonObject) Json.decodeValue(Buffer.buffer(in.readAllBytes()));
		}
		catch (IOException ioe) {
			return null;
		}
	}

	public static JsonArray getResourceAsJsonArray(String path) {
		try (InputStream in = getResourceAsStream(path)) {
			return (JsonArray) Json.decodeValue(Buffer.buffer(in.readAllBytes()));
		}
		catch (IOException ioe) {
			return null;
		}
	}
}
