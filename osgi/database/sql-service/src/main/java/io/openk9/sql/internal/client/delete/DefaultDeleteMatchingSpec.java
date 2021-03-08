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

package io.openk9.sql.internal.client.delete;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import io.openk9.sql.api.client.CriteriaDefinition;
import io.openk9.sql.api.client.DatabaseClient;
import io.openk9.sql.api.client.FetchSpec;
import io.openk9.sql.api.client.RowsFetchSpec;
import io.openk9.sql.internal.client.insert.DefaultGenericInsertSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;

import static io.openk9.sql.internal.client.util.DatabaseClientUtil.safeConnection;

public class DefaultDeleteMatchingSpec
	implements DatabaseClient.DeleteMatchingSpec, DatabaseClient.DeleteSpec {

	public DefaultDeleteMatchingSpec(
		ConnectionFactory connectionFactory, String table) {
		_connectionFactory = connectionFactory;
		_table = table;
		_criteria = CriteriaDefinition.empty();
	}

	public DefaultDeleteMatchingSpec(
		ConnectionFactory connectionFactory, String table,
		CriteriaDefinition criteria) {
		_connectionFactory = connectionFactory;
		_table = table;
		_criteria = criteria;
	}

	@Override
	public DatabaseClient.DeleteSpec matching(CriteriaDefinition criteria) {
		return new DefaultDeleteMatchingSpec(
			_connectionFactory, _table, criteria);
	}

	@Override
	public Mono<Void> then() {
		return _executeQuery().then();
	}

	@Override
	public <R> RowsFetchSpec<R> map(
		Function<Row, R> mappingFunction) {

		return new DefaultDeleteRowsFetchSpec<>(
			(row, rowMetadata) -> mappingFunction.apply(row), true);
	}

	@Override
	public <R> RowsFetchSpec<R> map(
		BiFunction<Row, RowMetadata, R> mappingFunction) {

		return new DefaultDeleteRowsFetchSpec<>(mappingFunction, true);
	}

	@Override
	public <R> RowsFetchSpec<R> map(Class<R> clazz) {

		//TODO to be implemented

		return null;
	}

	@Override
	public Mono<Integer> rowsUpdated() {
		return _executeQuery().count().map(Long::intValue);
	}

	private Flux<?> _executeQuery() {

		String query = _createQuery();

		if (_log.isDebugEnabled()) {
			_log.debug("executeQuery: " + query);
		}

		return safeConnection(
			_connectionFactory,
			connection -> connection
				.createStatement(query)
				.execute()
		);
	}

	private String _createQuery() {

		return String.format(
			"DELETE FROM %s WHERE %s;",
			_table, _criteria.toString());
	}

	class DefaultDeleteRowsFetchSpec<R> implements FetchSpec<R> {

		public DefaultDeleteRowsFetchSpec(
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

			return Mono
				.from(
					DefaultDeleteMatchingSpec.this._connectionFactory.create())
				.flatMapMany(connection ->
					Mono.from(
						connection
							.createStatement(query)
							.execute())
						.doFinally(signalType ->
							Mono.from(connection.close()).subscribe()))
				.flatMap(result -> result.map(_mappingFunction));
		}

		private String _createQuery() {

			String returning = _returning ? "RETURNING * " : "";

			return String.format(
				"DELETE FROM %s WHERE %s %s",
				_table, _criteria.toString(), returning);
		}

		private final boolean _returning;

		private String query;

		private final BiFunction<Row, RowMetadata, R> _mappingFunction;

	}

	private final ConnectionFactory _connectionFactory;

	private final String _table;

	private final CriteriaDefinition _criteria;

	private static final Logger _log = LoggerFactory.getLogger(
		DefaultGenericInsertSpec.class);



}
