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

package io.openk9.sql.internal.client.insert;

import io.openk9.sql.internal.client.util.DatabaseClientUtil;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import io.openk9.sql.api.client.DatabaseClient;
import io.openk9.sql.api.client.FetchSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultGenericInsertSpec
	implements DatabaseClient.GenericInsertSpec<Map<String, Object>> {

	public DefaultGenericInsertSpec(
		ConnectionFactory connectionFactory, String table) {
		_connectionFactory = connectionFactory;
		_table = table;
		_fieldValue = Collections.emptyMap();
	}

	public DefaultGenericInsertSpec(
		ConnectionFactory connectionFactory, String table,
		Map<String, Object> fieldValue) {
		_connectionFactory = connectionFactory;
		_table = table;
		_fieldValue = fieldValue;
	}

	@Override
	public <R> FetchSpec<R> map(Function<Row, R> mappingFunction) {
		return new DefaultInsertRowsFetchSpec<>(
			(row, ignore) -> mappingFunction.apply(row));
	}

	@Override
	public <R> FetchSpec<R> map(
		BiFunction<Row, RowMetadata, R> mappingFunction) {
		return new DefaultInsertRowsFetchSpec<>(mappingFunction);
	}

	@Override
	public FetchSpec<Map<String, Object>> fetch() {
		return new DefaultInsertRowsFetchSpec<>(
			(row, rowMetadata) ->
				rowMetadata
					.getColumnNames()
					.stream()
					.collect(Collectors.toMap(Function.identity(), row::get))
		);
	}

	@Override
	public Mono<Void> then() {
		return new DefaultInsertRowsFetchSpec<>(
			(row, rowMetadata) -> row, false)
			.rowsUpdated().then();
	}

	@Override
	public DatabaseClient.GenericInsertSpec<Map<String, Object>> value(
		String field, Object value) {

		Map<String, Object> newFieldValue = new LinkedHashMap<>(_fieldValue);

		newFieldValue.put(field, value);

		return new DefaultGenericInsertSpec(
			_connectionFactory, _table, newFieldValue);
	}

	@Override
	public DatabaseClient.GenericInsertSpec<Map<String, Object>> value(
		Map<String, Object> fieldValues) {

		Map<String, Object> newFieldValue = new LinkedHashMap<>(_fieldValue);

		newFieldValue.putAll(fieldValues);

		return new DefaultGenericInsertSpec(
			_connectionFactory, _table, newFieldValue);
	}

	private final ConnectionFactory _connectionFactory;
	private final String _table;
	private final Map<String, Object> _fieldValue;

	class DefaultInsertRowsFetchSpec<R> implements FetchSpec<R> {

		private final boolean _returning;

		public DefaultInsertRowsFetchSpec(
			BiFunction<Row, RowMetadata, R> mappingFunction) {
			_mappingFunction = mappingFunction;
			_returning = true;
		}

		public DefaultInsertRowsFetchSpec(
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

			return DatabaseClientUtil.safeConnection(
				_connectionFactory,
				connection ->
					Flux.from(
						DatabaseClientUtil.bind(_fieldValue, connection.createStatement(query))
							.execute())
						.flatMap(result -> result.map(_mappingFunction))
			);

		}

		private String _createQuery() {

			Map<String, Object> fieldValue =
				DefaultGenericInsertSpec.this._fieldValue;

			StringBuilder values = new StringBuilder();

			for (int i = 1; i <= fieldValue.size(); i++) {
				values.append("$").append(i);
			}

			String returning = _returning ? "RETURNING * " : "";

			return String.format(
				"INSERT INTO %s (%s) VALUES (%s) %s",
				DefaultGenericInsertSpec.this._table,
				String.join(",", fieldValue.keySet()),
				values.toString(),
				returning);
		}

		private String query;

		private final BiFunction<Row, RowMetadata, R> _mappingFunction;

	}

	private static final Logger _log = LoggerFactory.getLogger(
		DefaultGenericInsertSpec.class);

}