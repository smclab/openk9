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

import com.openk9.http.web.Cookie;
import com.openk9.http.web.HttpHandler;
import com.openk9.http.web.HttpMessage;
import com.openk9.http.web.HttpRequest;
import com.openk9.http.web.body.FileUpload;
import com.openk9.internal.http.body.FileUploadWrapper;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.ReferenceCountUtil;
import io.vavr.CheckedFunction1;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class HttpRequestImpl implements HttpRequest {

	private final Map<String, List<String>> _queryParams;

	public HttpRequestImpl(HttpServerRequest request) {
		_httpServerRequest = request;
		_queryParams = Collections.unmodifiableMap(
			new QueryStringDecoder(request.uri()).parameters());
	}

	@Override
	public String pathParam(CharSequence key) {
		return _httpServerRequest.param(key);
	}

	@Override
	public Map<String, String> pathParams() {

		Map<String, String> params = _httpServerRequest.params();

		return params == null ? Collections.emptyMap() : params;
	}

	@Override
	public Optional<String> firstParam(String key) {
		return _queryParams
			.getOrDefault(key, Collections.emptyList())
			.stream()
			.findFirst();
	}

	@Override
	public List<String> params(String key) {
		return Collections.unmodifiableList(
			_queryParams.getOrDefault(key, Collections.emptyList()));
	}

	@Override
	public Map<String, List<String>> params() {
		return new HashMap<>(_queryParams);
	}

	@Override
	public Iterable<Map.Entry<String, String>> requestHeaders() {
		return _httpServerRequest.requestHeaders();
	}

	@Override
	public Mono<Map<String, List<String>>> bodyAttributes() {
		return bodyAttributes(Attribute::getValue);
	}

	@Override
	public Mono<Map<String, String>> bodyAttributesFirst() {
		return bodyAttributes()
			.map(map -> map
				.entrySet()
				.stream()
				.map(entry -> new AbstractMap.SimpleEntry<>(
					entry.getKey(),
					entry.getValue().stream().findFirst().orElse(null)))
				.filter(entry -> entry.getValue() != null)
				.collect(
					Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
			);
	}

	@Override
	public Mono<List<String>> bodyAttribute(String key) {
		return bodyAttributes()
			.map(map -> map.getOrDefault(key, Collections.emptyList()));
	}

	@Override
	public Mono<Optional<String>> bodyAttributeFirst(String key) {
		return bodyAttribute(key)
			.map(attributes -> attributes.stream().findFirst());
	}

	@Override
	public Mono<String> bodyAttributeFirst(String key, String defaultValue) {
		return bodyAttributeFirst(key)
			.map(optional -> optional.orElse(defaultValue));
	}

	@Override
	public Mono<Map<String, List<byte[]>>> bodyAttributesBytes() {
		return bodyAttributes(Attribute::get);
	}

	@Override
	public Mono<Map<String, byte[]>> bodyAttributesBytesFirst() {
		return bodyAttributesBytes()
			.map(map -> map
				.entrySet()
				.stream()
				.map(entry -> new AbstractMap.SimpleEntry<>(
					entry.getKey(),
					entry.getValue().stream().findFirst().orElse(null)))
				.filter(entry -> entry.getValue() != null)
				.collect(
					Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
			);
	}

	@Override
	public Mono<List<byte[]>> bodyAttributeBytes(String key) {
		return bodyAttributesBytes()
			.map(map -> map.getOrDefault(key, Collections.emptyList()));
	}

	@Override
	public Mono<Optional<byte[]>> bodyAttributeBytesFirst(String key) {
		return bodyAttributeBytes(key)
			.map(attributes -> attributes.stream().findFirst());
	}

	@Override
	public Mono<byte[]> bodyAttributeBytesFirst(
		String key, byte[] defaultValue) {
		return bodyAttributeBytesFirst(key)
			.map(optional -> optional.orElse(defaultValue));
	}

	public <T> Mono<Map<String, List<T>>> bodyAttributes(
		CheckedFunction1<Attribute, T> mapper) {
		return getInterfaceHttpData()
			.map(list -> list
				.stream()
				.filter(
					e -> e.getHttpDataType()
						 == InterfaceHttpData.HttpDataType.Attribute)
				.map(e -> (Attribute) e)
				.collect(
					Collectors.groupingBy(
						InterfaceHttpData::getName,
						Collectors.collectingAndThen(
							Collectors.toList(),
							attributes ->
								attributes
									.stream()
									.map(mapper.unchecked())
									.collect(Collectors.toList())
						))
					));
	}

	@Override
	public Publisher<List<FileUpload>> fileUploads() {
		return fileUploads(false);
	}

	@Override
	public Publisher<List<FileUpload>> fileUploads(boolean useDisk) {
		return getInterfaceHttpData()
			.map(
				list -> list.stream()
					.filter(e ->
						e.getHttpDataType()
						== InterfaceHttpData.HttpDataType.FileUpload)
					.map(e ->
						(io.netty.handler.codec.http.multipart.FileUpload)e)
					.map(FileUploadWrapper::new)
					.collect(Collectors.toList()));
	}

	public Mono<List<InterfaceHttpData>> getInterfaceHttpData() {

		HttpPostRequestDecoder postDecoder =
			new HttpPostRequestDecoder(
				new DefaultHttpDataFactory(false),
				new HttpRequestWrapper(_httpServerRequest));

		return _httpServerRequest
			.receiveContent()
			.doOnNext(postDecoder::offer)
			.then(Mono.fromSupplier(postDecoder::getBodyHttpDatas))
			.doFinally(signal -> {
				postDecoder
					.getBodyHttpDatas()
					.forEach(ReferenceCountUtil::release);
				postDecoder.destroy();
			});

	}

	@Override
	public String scheme() {
		return _httpServerRequest.scheme();
	}

	@Override
	public InetSocketAddress hostAddress() {
		return _httpServerRequest.hostAddress();
	}

	@Override
	public InetSocketAddress remoteAddress() {
		return _httpServerRequest.remoteAddress();
	}

	@Override
	public Map<CharSequence, Set<Cookie>> cookies() {
		//return _httpServerRequest.cookies(); TODO da implementare
		return Collections.emptyMap();
	}

	@Override
	public boolean isKeepAlive() {
		return _httpServerRequest.isKeepAlive();
	}

	@Override
	public boolean isWebsocket() {
		return _httpServerRequest.isWebsocket();
	}

	@Override
	public int method() {
		return _httpServerRequest.method() == HttpMethod.GET ? HttpHandler.GET
			: _httpServerRequest.method() == HttpMethod.POST ? HttpHandler.POST
			: _httpServerRequest.method() == HttpMethod.PATCH ? HttpHandler.PATCH
			: _httpServerRequest.method() == HttpMethod.PUT ? HttpHandler.PUT
			: _httpServerRequest.method() == HttpMethod.DELETE ? HttpHandler.DELETE
			: _httpServerRequest.method() == HttpMethod.OPTIONS ? HttpHandler.OPTIONS
			: 0;
	}

	@Override
	public String uri() {
		return _httpServerRequest.uri();
	}

	@Override
	public String version() {
		return _httpServerRequest.version().text();
	}

	@Override
	public Publisher<HttpMessage> receive() {
		return _httpServerRequest
			.receive()
			.retain()
			.map(HttpMessageImpl::new);
	}

	@Override
	public Publisher<byte[]> aggregateBodyToByteArray() {
		return _httpServerRequest
			.receive()
			.aggregate()
			.asByteArray();
	}

	@Override
	public Publisher<String> aggregateBodyToString() {
		return _httpServerRequest
			.receive()
			.aggregate()
			.asString();
	}

	@Override
	public Publisher<InputStream> aggregateBodyToInputStream() {
		return _httpServerRequest
			.receive()
			.aggregate()
			.asInputStream();
	}

	public HttpServerRequest getHttpServerRequest() {
		return _httpServerRequest;
	}

	private final HttpServerRequest _httpServerRequest;

}
