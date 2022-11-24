package io.openk9.datasource.jackson.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;

public class TemporalAccessorToMillisecondsSerializer
	extends StdSerializer<TemporalAccessor> {

	public TemporalAccessorToMillisecondsSerializer() {
		this(null);
	}

	public TemporalAccessorToMillisecondsSerializer(Class<TemporalAccessor> t) {
		super(t);
	}

	@Override
	public void serialize(
		TemporalAccessor value, JsonGenerator gen, SerializerProvider provider)
		throws IOException {

		if (value == null) {
			gen.writeNull();
		}
		else {
			gen.writeNumber(Instant.from(value).toEpochMilli());
		}

	}

}
