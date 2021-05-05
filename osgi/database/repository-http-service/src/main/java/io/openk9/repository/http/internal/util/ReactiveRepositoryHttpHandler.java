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

package io.openk9.repository.http.internal.util;

import io.openk9.common.api.constant.Strings;
import io.openk9.documentation.model.RestDocumentation;
import io.openk9.http.util.BaseEndpointRegister;
import io.openk9.http.util.HttpHandlerUtil;
import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.web.Endpoint;
import io.openk9.http.web.HttpHandler;
import io.openk9.http.web.HttpRequest;
import io.openk9.http.web.HttpResponse;
import io.openk9.json.api.JsonFactory;
import io.openk9.repository.http.internal.http.HttpRequestUtil;
import io.openk9.sql.api.client.Criteria;
import io.openk9.sql.api.client.Page;
import io.openk9.sql.api.entity.ReactiveRepository;
import org.osgi.framework.BundleContext;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
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
				this::_patch),
			HttpHandler.post("/filter", this::_filter)
		);

	}

	private Publisher<Void> _filter(
		HttpRequest httpRequest, HttpResponse httpResponse) {

		Publisher<String> body = httpRequest.aggregateBodyToString();

		return _httpResponseWriter.write(
			httpResponse,
			Mono
				.from(body)
				.map(json -> _jsonFactory.fromJsonMap(json, Object.class))
				.map(this::_createCriteria)
				.flatMapMany(_reactiveRepository::findBy)
		);

	}

	private Criteria _createCriteria(Map<String, Object> fieldValues) {

		Set<Map.Entry<String, Object>> entries = fieldValues.entrySet();

		Criteria criteria = Criteria.empty();

		for (Map.Entry<String, Object> entry : entries) {

			Object value = entry.getValue();

			String key = entry.getKey();

			boolean not = false;

			if (key.endsWith("_not")) {
				not = true;
				key = key.replace("_not", Strings.BLANK);
			}

			Criteria.CriteriaStep where = Criteria.where(key);

			Criteria innerCriteria;

			if (not) {
				innerCriteria = _getNotCriteria(value, where);
			}
			else {
				innerCriteria = _getCriteria(value, where);
			}

			if (criteria.isEmpty()) {
				criteria = innerCriteria;
			}
			else {
				criteria = criteria.and(innerCriteria);
			}

		}

		return criteria;
	}

	private Criteria _getCriteria(Object value, Criteria.CriteriaStep where) {

		if (value == null) {
			return where.isNull();
		}
		else if (value instanceof Boolean) {
			return ((Boolean) value)
				? where.isTrue()
				: where.isFalse();
		}
		else if (value instanceof Object[]) {
			return where.in(Arrays.asList((Object[]) value));
		}
		else if (value instanceof Collection) {
			return where.in((Collection<?>) value);
		}
		else {
			return where.is(value);
		}

	}

	private Criteria _getNotCriteria(
		Object value, Criteria.CriteriaStep where) {

		if (value == null) {
			return where.isNotNull();
		}
		else if (value instanceof Boolean) {
			return ((Boolean) value)
				? where.isFalse()
				: where.isTrue();
		}
		else if (value instanceof Object[]) {
			return where.notIn(Arrays.asList((Object[]) value));
		}
		else if (value instanceof Collection) {
			return where.notIn((Collection<?>) value);
		}
		else {
			return where.not(value);
		}

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
