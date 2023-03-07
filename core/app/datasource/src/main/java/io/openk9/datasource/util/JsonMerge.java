package io.openk9.datasource.util;

import com.jayway.jsonpath.JsonPath;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

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
