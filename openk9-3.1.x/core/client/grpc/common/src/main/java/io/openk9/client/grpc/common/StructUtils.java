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

package io.openk9.client.grpc.common;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;

import java.io.IOException;
import java.io.Reader;

public class StructUtils {

	public static Struct fromJson(String json) {

		try {
			var structBuilder = Struct.newBuilder();
			JsonFormat.parser().merge(json != null ? json : "{}", structBuilder);
			return structBuilder.build();
		}
		catch (InvalidProtocolBufferException e) {
			throw new JsonParseException(e);
		}

	}

	public static Struct fromJson(Reader json) {

		try {
			var structBuilder = Struct.newBuilder();
			JsonFormat.parser().merge(json != null ? json : Reader.nullReader(), structBuilder);
			return structBuilder.build();
		}
		catch (IOException e) {
			throw new JsonParseException(e);
		}

	}

	public static String toJsonString(Struct struct) {
		try {
			return JsonFormat.printer().print(struct);
		}
		catch (InvalidProtocolBufferException e) {
			throw new JsonPrinterException(e);
		}
	}

}
