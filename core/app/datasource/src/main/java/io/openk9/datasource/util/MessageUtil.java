package io.openk9.datasource.util;

import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Message;

public class MessageUtil {

	public static JsonObject toJsonObject(Message<?> message) {

		Object payload = message.getPayload();

		if (payload instanceof JsonObject) {
			return (JsonObject) payload;
		}

		if (payload instanceof String) {
			return new JsonObject((String) payload);
		}

		if (payload instanceof byte[]) {
			return new JsonObject(new String((byte[]) payload));
		}

		throw new IllegalArgumentException("payload not supported");

	}

	public static <T> T toObj(Message<?> message, Class<T> clazz) {

		Object payload = message.getPayload();

		if (clazz.isInstance(payload)) {
			return clazz.cast(payload);
		}

		return toJsonObject(message).mapTo(clazz);

	}

}
