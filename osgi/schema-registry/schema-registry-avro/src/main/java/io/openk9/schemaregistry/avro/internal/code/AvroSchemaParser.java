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

package io.openk9.schemaregistry.avro.internal.code;

import io.openk9.schemaregistry.codec.SchemaDeserializer;
import io.openk9.schemaregistry.codec.SchemaParser;
import io.openk9.schemaregistry.codec.SchemaSerializer;
import org.apache.avro.Schema;
import org.apache.avro.data.TimeConversions;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component(
	immediate = true,
	property = {
		"format=avro"
	},
	service = {
		SchemaDeserializer.class,
		SchemaSerializer.class,
		SchemaParser.class
	}
)
public class AvroSchemaParser implements SchemaParser {

	@Override
	public <T> T deserialize(String definition, byte[] data, Class<T> clazz) {

		Schema parse = new Schema.Parser().parse(definition);

		ReflectData reflectData = new ReflectData(clazz.getClassLoader());

		reflectData.addLogicalTypeConversion(
			new TimeConversions.LocalTimestampMillisConversion());

		DatumReader<T> reader = new ReflectDatumReader<>(
			parse, parse, reflectData);


		try {
			Decoder decoder = DecoderFactory.get().binaryDecoder(
				new ByteArrayInputStream(data), null);

			return reader.read(null, decoder);
		} catch (IOException e) {
			_log.error("Deserialization error:" + e.getMessage(), e);
		}

		return null;
	}

	@Override
	public <T> T deserializeJson(
		String definition, byte[] data, Class<T> clazz) {
		return null;
	}

	@Override
	public <T> byte[] serialize(
		String definition, T object, Class<T> clazz) {

		Schema parse = new Schema.Parser().parse(definition);

		ReflectData reflectData = new ReflectData(clazz.getClassLoader());

		reflectData.addLogicalTypeConversion(
			new TimeConversions.LocalTimestampMillisConversion());

		DatumWriter<T> writer = new ReflectDatumWriter<>(
			parse, reflectData);

		byte[] data = new byte[0];

		ByteArrayOutputStream stream = new ByteArrayOutputStream();

		try {

			Encoder jsonEncoder = EncoderFactory.get().binaryEncoder(
				stream, null);

			writer.write(object, jsonEncoder);

			jsonEncoder.flush();

			data = stream.toByteArray();

		} catch (IOException e) {
			_log.error("Serialization error:" + e.getMessage());
		}

		return data;
	}

	@Override
	public <T> byte[] serializeJson(
		String definition, T object, Class<T> clazz) {
		return new byte[0];
	}

	private static final Logger _log = LoggerFactory.getLogger(
		AvroSchemaParser.class);

}
