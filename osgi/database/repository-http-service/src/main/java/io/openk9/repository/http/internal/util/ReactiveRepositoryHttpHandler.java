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
import io.openk9.http.util.HttpResponseWriter;
import io.openk9.http.web.RouterHandler;
import io.openk9.json.api.JsonFactory;
import io.openk9.reactor.netty.util.HttpPredicateV2;
import io.openk9.reactor.netty.util.ReactorNettyUtils;
import io.openk9.sql.api.client.Criteria;
import io.openk9.sql.api.entity.ReactiveRepository;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;
import reactor.netty.http.server.HttpServerRoutes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReactiveRepositoryHttpHandler implements RouterHandler {

	public ReactiveRepositoryHttpHandler(
		String _basePath,
		ReactiveRepository<Object, Object> _reactiveRepository,
		JsonFactory _jsonFactory, HttpResponseWriter _httpResponseWriter) {

		this._basePath = _basePath;
		this._reactiveRepository = _reactiveRepository;
		this._jsonFactory = _jsonFactory;
		this._httpResponseWriter = _httpResponseWriter;
	}

	@Override
	public HttpServerRoutes handle(HttpServerRoutes router) {

		return router
			.get(_basePath, this::_findAll)
			.get(_basePath + "/{" + _reactiveRepository.primaryKeyName() + "}",
				this::_findByPrimaryKey)
			.delete(_basePath + "/{" + _reactiveRepository.primaryKeyName() + "}", this::_delete)
			.post(_basePath, this::_create)
			.put(_basePath, this::_update)
			.route(
				HttpPredicateV2.patch(_basePath + "/{" + _reactiveRepository.primaryKeyName() + "}"),
				this::_patch)
			.post(_basePath + "/filter", this::_filter);
	}

	private Publisher<Void> _filter(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		Publisher<String> body = ReactorNettyUtils.aggregateBodyAsString(
			httpRequest);

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

	private Publisher<Void> _patch(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		String primaryKey =
			httpRequest.param(_reactiveRepository.primaryKeyName());

		Object PK = _reactiveRepository.parsePrimaryKey(primaryKey);

		Publisher<String> body = ReactorNettyUtils.aggregateBodyAsString(
			httpRequest);

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
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		Publisher<String> body = ReactorNettyUtils.aggregateBodyAsString(
			httpRequest);;

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
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		Publisher<String> body = ReactorNettyUtils.aggregateBodyAsString(
			httpRequest);

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
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		String primaryKey =
			httpRequest.param(_reactiveRepository.primaryKeyName());

		Object PK = _reactiveRepository.parsePrimaryKey(primaryKey);

		return _httpResponseWriter.write(
			httpResponse, _reactiveRepository.delete(PK));
	}

	private Publisher<Void> _findByPrimaryKey(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		String primaryKey =
			httpRequest.param(_reactiveRepository.primaryKeyName());

		Object PK = _reactiveRepository.parsePrimaryKey(primaryKey);

		return _httpResponseWriter.write(
			httpResponse, _reactiveRepository.findByPrimaryKey(PK));
	}

	private Publisher<Void> _findAll(
		HttpServerRequest httpRequest, HttpServerResponse httpResponse) {

		return _httpResponseWriter.write(
			httpResponse, _reactiveRepository.findAll());
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
