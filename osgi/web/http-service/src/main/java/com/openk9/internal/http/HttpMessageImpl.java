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

package com.openk9.internal.http;

import com.openk9.http.web.HttpMessage;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HttpMessageImpl implements HttpMessage {

	public HttpMessageImpl(ByteBuf byteBuf) {
		_byteBuf = byteBuf;
	}

	@Override
	public String getPayloadAsString(Charset charset) {
		return _byteBuf.toString(charset);
	}

	@Override
	public String getPayloadAsString() {
		return _byteBuf.toString(StandardCharsets.UTF_8);
	}

	@Override
	public ByteBuffer getPayload() {
		return _byteBuf.nioBuffer();
	}

	public ByteBuf getByteBuf() {
		return _byteBuf.duplicate().retain();
	}

	private final ByteBuf _byteBuf;
}
