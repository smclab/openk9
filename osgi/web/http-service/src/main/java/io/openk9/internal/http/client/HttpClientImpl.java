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

package io.openk9.internal.http.client;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.openk9.http.client.HttpClient;
import io.openk9.http.exception.HttpException;
import io.openk9.http.web.HttpHandler;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.ByteBufMono;
import reactor.netty.http.client.HttpClientResponse;

import java.util.Collections;
import java.util.Map;

class HttpClientImpl implements HttpClient {


	HttpClientImpl(reactor.netty.http.client.HttpClient httpClient) {
		_httpClient = httpClient;
	}

	@Override
	public Mono<byte[]> request(int method, String url) {
		return request(method, url, Collections.emptyMap());
	}

	@Override
	public Mono<byte[]> request(
		int method, String url, Map<String, String> formDataAttr) {
		return request(method, url, formDataAttr, Collections.emptyMap());
	}

	@Override
	public Publisher<byte[]> request(
		int method, String url, String dataRow, Map<String, Object> headers) {

		reactor.netty.http.client.HttpClient requestHttpClient = _httpClient;

		if (!headers.isEmpty()) {
			requestHttpClient =
				requestHttpClient
					.headers(entries -> headers.forEach(entries::add));
		}

		reactor.netty.http.client.HttpClient.RequestSender request =
			requestHttpClient
				.followRedirect(false)
				.request(_findHttpMethod(method));

		return request
			.send(ByteBufMono.fromString(Mono.just(dataRow)))
			.uri(url)
			.responseSingle(HttpClientImpl::_response);
	}

	@Override
	public Mono<byte[]> request(
		int method, String url, Map<String, String> formDataAttr,
		Map<String, Object> headers) {

		reactor.netty.http.client.HttpClient requestHttpClient = _httpClient;

		if (!headers.isEmpty()) {
			requestHttpClient =
				requestHttpClient
					.headers(entries -> headers.forEach(entries::add));
		}

		reactor.netty.http.client.HttpClient.RequestSender request =
			requestHttpClient
				.followRedirect(false)
				.request(_findHttpMethod(method));

		reactor.netty.http.client.HttpClient.ResponseReceiver<?> receiver =
			request;

		if (!formDataAttr.isEmpty()) {
			receiver = request.sendForm(
				(httpClientRequest, httpClientForm) ->
					formDataAttr.forEach(httpClientForm::attr));
		}

		return receiver
			.uri(url)
			.responseSingle(HttpClientImpl::_response);

	}

	private static Mono<byte[]> _response(
		HttpClientResponse response, ByteBufMono mono) {

		HttpResponseStatus status = response.status();

		if (status.code() != 200) {
			return Mono.error(
				new HttpException(
					status.code(), status.reasonPhrase(),
					mono.asString()));
		}

		return mono.asByteArray();

	}

	private HttpMethod _findHttpMethod(int method) {

		return method == HttpHandler.GET ? HttpMethod.GET
			: method == HttpHandler.POST ? HttpMethod.POST
			: method == HttpHandler.PATCH ? HttpMethod.PATCH
			: method == HttpHandler.PUT ? HttpMethod.PUT
			: method == HttpHandler.DELETE ? HttpMethod.DELETE
			: method == HttpHandler.OPTIONS ? HttpMethod.OPTIONS
			: HttpMethod.GET;
	}

	private final reactor.netty.http.client.HttpClient _httpClient;

}
