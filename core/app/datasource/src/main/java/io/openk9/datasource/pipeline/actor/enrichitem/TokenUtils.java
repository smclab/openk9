package io.openk9.datasource.pipeline.actor.enrichitem;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;

import java.util.Base64;

public class TokenUtils {

	public static String encode(Token.SchedulationToken token) {
		String jsonToken = Json.encode(token);
		return Base64.getEncoder().encodeToString(jsonToken.getBytes());
	}

	public static Token.SchedulationToken decode(String value) {
		byte[] jsonToken = Base64.getDecoder().decode(value);
		return Json.decodeValue(Buffer.buffer(jsonToken), Token.SchedulationToken.class);
	}

}
