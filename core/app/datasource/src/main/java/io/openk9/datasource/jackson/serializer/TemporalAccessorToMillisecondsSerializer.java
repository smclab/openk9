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

package io.openk9.datasource.jackson.serializer;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

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
