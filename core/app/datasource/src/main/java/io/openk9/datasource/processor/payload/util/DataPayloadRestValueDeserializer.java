package io.openk9.datasource.processor.payload.util;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.vertx.core.json.Json;

import java.io.IOException;

public class DataPayloadRestValueDeserializer extends StdDeserializer<Object> {

	protected DataPayloadRestValueDeserializer() {
		this(null);
	}

	protected DataPayloadRestValueDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctxt)
		throws IOException, JacksonException {

		String valueAsString = p.getValueAsString();

		return valueAsString == null ? null : Json.decodeValue(valueAsString);
	}
}
