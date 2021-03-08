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

package io.openk9.sql.internal.client.update;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import io.openk9.sql.api.client.CriteriaDefinition;
import io.openk9.sql.api.client.DatabaseClient;
import io.openk9.sql.api.client.FetchSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.openk9.sql.internal.client.util.DatabaseClientUtil.bind;
import static io.openk9.sql.internal.client.util.DatabaseClientUtil.safeConnection;

public class DefaultGenericUpdateSpec<T>
	implements DatabaseClient.GenericUpdateSpec<T> {

	public DefaultGenericUpdateSpec(
		ConnectionFactory connectionFactory, String table) {
		_connectionFactory = connectionFactory;
		_table = table;
		_fieldValue = Collections.emptyMap();
		_criteria = CriteriaDefinition.empty();
	}

	public DefaultGenericUpdateSpec(
		ConnectionFactory connectionFactory, String table,
		Map<String, Object> fieldValue, CriteriaDefinition criteriaDefinition) {
		_connectionFactory = connectionFactory;
		_table = table;
		_fieldValue = fieldValue;
		_criteria = criteriaDefinition;
	}

	@Override
	public <R> FetchSpec<R> map(Function<Row, R> mappingFunction) {
		return new DefaultUpdateRowsFetchSpec<>(
			(row, ignore) -> mappingFunction.apply(row));
	}

	@Override
	public <R> FetchSpec<R> map(
		BiFunction<Row, RowMetadata, R> mappingFunction) {
		return new DefaultUpdateRowsFetchSpec<>(mappingFunction);
	}

	@Override
	public FetchSpec<Map<String, Object>> fetch() {
		return new DefaultUpdateRowsFetchSpec<>(
			(row, rowMetadata) ->
				rowMetadata
					.getColumnNames()
					.stream()
					.collect(Collectors.toMap(Function.identity(), row::get))
		);
	}

	@Override
	public Mono<Void> then() {
		return new DefaultUpdateRowsFetchSpec<>(
			(row, rowMetadata) -> row, false)
			.rowsUpdated().then();
	}

	@Override
	public DatabaseClient.GenericUpdateSpec<T> value(
		String field, Object value) {

		Map<String, Object> newFieldValue = new LinkedHashMap<>(_fieldValue);

		newFieldValue.put(field, value);

		return new DefaultGenericUpdateSpec<>(
			_connectionFactory, _table, newFieldValue, _criteria);
	}

	@Override
	public DatabaseClient.GenericUpdateSpec<T> value(
		Map<String, Object> fieldValues) {

		Map<String, Object> newFieldValue = new LinkedHashMap<>(_fieldValue);

		newFieldValue.putAll(fieldValues);

		return new DefaultGenericUpdateSpec<>(
			_connectionFactory, _table, newFieldValue, _criteria);

	}

	@Override
	public DatabaseClient.UpdateSpec<T> matching(
		CriteriaDefinition criteria) {
		return new DefaultGenericUpdateSpec<>(
			_connectionFactory, _table, _fieldValue, criteria);
	}

	private final ConnectionFactory _connectionFactory;
	private final String _table;
	private final Map<String, Object> _fieldValue;
	private final CriteriaDefinition _criteria;

	class DefaultUpdateRowsFetchSpec<R> implements FetchSpec<R> {

		public DefaultUpdateRowsFetchSpec(
			BiFunction<Row, RowMetadata, R> mappingFunction) {
			_mappingFunction = mappingFunction;
			_returning = true;
		}

		public DefaultUpdateRowsFetchSpec(
			BiFunction<Row, RowMetadata, R> mappingFunction,
			boolean returning) {
			_mappingFunction = mappingFunction;
			_returning = returning;
		}

		@Override
		public Mono<R> one() {
			return _executeQuery().next();
		}

		@Override
		public Mono<R> first() {
			return _executeQuery().next();
		}

		@Override
		public Flux<R> all() {
			return _executeQuery();
		}

		@Override
		public Mono<Integer> rowsUpdated() {
			return _executeQuery()
				.count()
				.map(Long::intValue);
		}

		private Flux<R> _executeQuery() {
			if (query == null) {
				query = _createQuery();
			}

			if (_log.isDebugEnabled()) {
				_log.debug("executeQuery: " + query);
			}

			return safeConnection(
				_connectionFactory, connection ->
					Flux.from(
						bind(
							_fieldValue, connection.createStatement(query))
							.execute())
						.flatMap(result -> result.map(_mappingFunction))
			);
		}

		private String _createQuery() {

			List<String> statements = new ArrayList<>(_fieldValue.size());

			int i = 1;

			for (String key : _fieldValue.keySet()) {
				statements.add(key + " = $" + i++);
			}

			String set = String.join(", ", statements);

			String where;

			if (!_criteria.isEmpty()) {
				where = " WHERE " + _criteria.toString();
			}
			else {
				where = "";
			}

			String returning = _returning ? "RETURNING * " : "";

			return String.format(
				"UPDATE %s SET %s %s %s",
				DefaultGenericUpdateSpec.this._table, set, where, returning);
		}

		private String query;
		private final BiFunction<Row, RowMetadata, R> _mappingFunction;
		private final boolean _returning;


	}

	private static final Logger _log = LoggerFactory.getLogger(
		DefaultGenericUpdateSpec.class);

}
