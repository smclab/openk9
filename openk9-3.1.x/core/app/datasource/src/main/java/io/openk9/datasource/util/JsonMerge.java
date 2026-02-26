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

import java.util.Objects;

import com.jayway.jsonpath.JsonPath;
import io.vertx.core.json.JsonObject;

public sealed abstract class JsonMerge {
	private static final String ROOT = "$";

	protected final JsonObject source;
	protected final JsonObject target;

	public JsonMerge(JsonObject source, JsonObject target) {
		this.source = Objects.requireNonNull(source, "source is null");
		this.target = Objects.requireNonNull(target, "target is null");
	}

	public JsonObject merge(String jsonPath) {
		return merge(JsonPath.compile(jsonPath));
	}

	public abstract JsonObject merge();
	public abstract JsonObject merge(JsonPath jsonPath);

	public static JsonMerge replace(JsonObject source, JsonObject target) {
		return new Replace(source, target);
	}

	public static JsonMerge merge(JsonObject source, JsonObject target) {
		return new Merge(source, target);
	}

	public static JsonMerge of(boolean replace, JsonObject source, JsonObject target) {
		return replace ? replace(source, target) : merge(source, target);
	}

	private final static class Merge extends JsonMerge {

		public Merge(JsonObject source, JsonObject target) {
			super(source, target);
		}

		@Override
		public JsonObject merge() {
			return new JsonObject().mergeIn(source).mergeIn(target, true);
		}

		@Override
		public JsonObject merge(JsonPath jsonPath) {
			JsonObject jsonObject = new JsonObject().mergeIn(source);

			String path = jsonPath.getPath();

			if (path.equals(ROOT)) {
				return merge();
			}

			Object valuePath = jsonPath.read(
				jsonObject, VertxJsonNodeJsonProvider.CONFIGURATION);

			if (valuePath instanceof JsonObject) {
				JsonObject valuePathJsonObject = (JsonObject) valuePath;
				valuePathJsonObject.mergeIn(target, true);
			}
			else {
				jsonPath.set(jsonObject, target, VertxJsonNodeJsonProvider.CONFIGURATION);
			}

			return jsonObject;
		}

	}

	private final static class Replace extends JsonMerge {

		public Replace(JsonObject source, JsonObject target) {
			super(source, target);
		}

		@Override
		public JsonObject merge() {
			return new JsonObject().mergeIn(source).mergeIn(target);
		}

		@Override
		public JsonObject merge(JsonPath jsonPath) {
			String path = jsonPath.getPath();
			if (path.equals(ROOT)) {
				return merge();
			}
			JsonObject jsonObject = new JsonObject().mergeIn(source);
			jsonPath.set(jsonObject, target, VertxJsonNodeJsonProvider.CONFIGURATION);
			return jsonObject;
		}

	}

}
