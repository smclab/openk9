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

package com.openk9.repository.http.internal.util;

import com.openk9.common.api.constant.Strings;
import com.openk9.documentation.model.RestDocumentation;
import com.openk9.http.util.BaseEndpointRegister;
import com.openk9.http.util.HttpHandlerUtil;
import com.openk9.http.util.HttpResponseWriter;
import com.openk9.http.web.Endpoint;
import com.openk9.http.web.HttpHandler;
import com.openk9.http.web.HttpRequest;
import com.openk9.http.web.HttpResponse;
import com.openk9.json.api.JsonFactory;
import com.openk9.repository.http.internal.http.HttpRequestUtil;
import com.openk9.sql.api.client.Page;
import com.openk9.sql.api.entity.ReactiveRepository;
import org.osgi.framework.BundleContext;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class ReactiveRepositoryHttpHandler extends BaseEndpointRegister {

	public ReactiveRepositoryHttpHandler(
		String _basePath,
		ReactiveRepository<Object, Object> _reactiveRepository,
		JsonFactory _jsonFactory, HttpResponseWriter _httpResponseWriter,
		BundleContext _bundleContext) {

		this._basePath = _basePath;
		this._reactiveRepository = _reactiveRepository;
		this._jsonFactory = _jsonFactory;
		this._httpResponseWriter = _httpResponseWriter;
		setBundleContext(_bundleContext);
	}

	@Override
	public String getBasePath() {
		return _basePath;
	}

	public void start() {

		registerEndpoint(
			HttpHandler.get(Strings.BLANK, this::_findAll),
			HttpHandler.get(
				"/{" + _reactiveRepository.primaryKeyName() + "}",
				this::_findByPrimaryKey),
			HttpHandler.delete(
				"/{" + _reactiveRepository.primaryKeyName() + "}",
				this::_delete),
			HttpHandler.post(Strings.BLANK, this::_create),
			HttpHandler.put(Strings.BLANK, this::_update),
			HttpHandler.patch(
				"/{" + _reactiveRepository.primaryKeyName() + "}",
				this::_patch)

		);

	}

	@Override
	protected BaseEndpointRegister registerEndpoint(
		Endpoint... endpoints) {

		for (Endpoint endpoint : endpoints) {

			if (endpoint instanceof HttpHandler) {

				HttpHandler httpHandler = (HttpHandler)endpoint;

				int method = httpHandler.method();

				String path = httpHandler.getPath();

				RestDocumentation.RestDocumentationBuilder restDoc =
					RestDocumentation
						.builder()
						.allowMethods(
							HttpHandlerUtil.bitsToMethodName(method));

				if (getBasePath() != null && !getBasePath().isEmpty()) {
					restDoc.path(getBasePath() + path);
				}
				else {
					restDoc.path(path);
				}

				if (method == HttpHandler.GET && path.equals(Strings.BLANK)) {
					restDoc.params(
						List.of(
							HttpRequestUtil._START,
							HttpRequestUtil._END,
							HttpRequestUtil._ORDER_COLUMN,
							HttpRequestUtil._ORDER_TYPE
						)
					);
				}

				restDoc.prefix(httpHandler.prefix());

				if ((method == HttpHandler.POST || method == HttpHandler.PUT)
					&& path.equals(Strings.BLANK)) {

					restDoc.body(
						_jsonFactory.toJsonClassDefinition(
							_reactiveRepository.entityClass()));

				}

				restDoc.response(
					_jsonFactory.toJsonClassDefinition(
						_reactiveRepository.entityClass()));

				_restDocumentationList.add(restDoc.build());

			}

		}

		return super.registerEndpoint(
			Stream.concat(
				Arrays.stream(endpoints),
				Stream.<Endpoint>of(
					HttpHandler.get(
						"/doc",
						(req, res) -> _httpResponseWriter.write(
							res, _restDocumentationList))
				)
			)
				.toArray(Endpoint[]::new)
		);

	}

	private Publisher<Void> _patch(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		String primaryKey =
			httpRequest.pathParam(_reactiveRepository.primaryKeyName());

		Object PK = _reactiveRepository.parsePrimaryKey(primaryKey);

		Publisher<String> body = httpRequest.aggregateBodyToString();

		return _httpResponseWriter.write(
			httpResponse,
			Mono
				.from(body)
				.map(
					json -> _jsonFactory.fromJsonMap(json, Object.class)
				)
				.flatMap(map -> _reactiveRepository.patch(PK, map)
			)
		);

	}

	private Publisher<Void> _update(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		Publisher<String> body = httpRequest.aggregateBodyToString();

		return _httpResponseWriter.write(
			httpResponse,
			_reactiveRepository.update(
				Mono
					.from(body)
					.map(
						json -> _jsonFactory.fromJson(
							json, _reactiveRepository.entityClass())
					)
			)
		);

	}

	private Publisher<Void> _create(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		Publisher<String> body = httpRequest.aggregateBodyToString();

		return _httpResponseWriter.write(
			httpResponse,
			_reactiveRepository.insert(
				Mono
					.from(body)
					.map(
						json -> _jsonFactory.fromJson(
							json, _reactiveRepository.entityClass())
					)
			)
		);

	}

	private Publisher<Void> _delete(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		String primaryKey =
			httpRequest.pathParam(_reactiveRepository.primaryKeyName());

		Object PK = _reactiveRepository.parsePrimaryKey(primaryKey);

		return _httpResponseWriter.write(
			httpResponse, _reactiveRepository.delete(PK));
	}

	private Publisher<Void> _findByPrimaryKey(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		String primaryKey =
			httpRequest.pathParam(_reactiveRepository.primaryKeyName());

		Object PK = _reactiveRepository.parsePrimaryKey(primaryKey);

		return _httpResponseWriter.write(
			httpResponse, _reactiveRepository.findByPrimaryKey(PK));
	}

	private Publisher<Void> _findAll(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		Page page = HttpRequestUtil.getPage(httpRequest);

		return _httpResponseWriter.write(
			httpResponse, _reactiveRepository.findAll(page));
	}

	public List<RestDocumentation> getRestDocumentationList() {
		return new ArrayList<>(_restDocumentationList);
	}

	private final String _basePath;

	private final ReactiveRepository<Object, Object> _reactiveRepository;

	private final JsonFactory _jsonFactory;

	private final HttpResponseWriter _httpResponseWriter;

	private final List<RestDocumentation> _restDocumentationList =
		new ArrayList<>();

}
