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

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import lombok.RequiredArgsConstructor;
import reactor.netty.http.server.HttpServerRequest;

@RequiredArgsConstructor
class HttpRequestWrapper implements HttpRequest {

	@Override
	public HttpMethod getMethod() {
		return _httpServerRequest.method();
	}

	@Override
	public HttpMethod method() {
		return _httpServerRequest.method();
	}

	@Override
	public HttpRequest setMethod(HttpMethod method) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getUri() {
		return _httpServerRequest.uri();
	}

	@Override
	public String uri() {
		return _httpServerRequest.uri();
	}

	@Override
	public HttpRequest setUri(String uri) {
		throw new UnsupportedOperationException();
	}

	@Override
	public HttpVersion getProtocolVersion() {
		return _httpServerRequest.version();
	}

	@Override
	public HttpVersion protocolVersion() {
		return _httpServerRequest.version();
	}

	@Override
	public HttpRequest setProtocolVersion(
		HttpVersion version) {
		throw new UnsupportedOperationException();
	}

	@Override
	public HttpHeaders headers() {
		return _httpServerRequest.requestHeaders();
	}

	@Override
	public DecoderResult getDecoderResult() {
		throw new UnsupportedOperationException();
	}

	@Override
	public DecoderResult decoderResult() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDecoderResult(DecoderResult result) {
		throw new UnsupportedOperationException();
	}

	private final HttpServerRequest _httpServerRequest;

}
