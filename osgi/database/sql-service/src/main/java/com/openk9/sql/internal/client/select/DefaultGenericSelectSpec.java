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

package com.openk9.sql.internal.client.select;

import com.openk9.sql.internal.client.util.DatabaseClientUtil;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import com.openk9.sql.api.client.CriteriaDefinition;
import com.openk9.sql.api.client.DatabaseClient;
import com.openk9.sql.api.client.Page;
import com.openk9.sql.api.client.RowsFetchSpec;
import com.openk9.sql.api.client.Sort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultGenericSelectSpec
	extends DefaultSelectSpec<DefaultGenericSelectSpec>
	implements DatabaseClient.GenericSelectSpec {

	public DefaultGenericSelectSpec(
		ConnectionFactory connectionFactory, String table) {
		super(connectionFactory, table);
	}

	public DefaultGenericSelectSpec(
		ConnectionFactory connectionFactory, String table,
		Collection<Sort> sorts, Page page, Collection<String> columns,
		CriteriaDefinition criteriaDefinition) {

		super(
			connectionFactory, table, sorts, page, columns, criteriaDefinition);
	}

	@Override
	public DefaultGenericSelectSpec createInstance(
		ConnectionFactory connectionFactory, String table,
		Collection<Sort> sorts, Page page, Collection<String> columns,
		CriteriaDefinition criteriaDefinition) {
		return new DefaultGenericSelectSpec(
			connectionFactory, table, sorts, page, columns,
			criteriaDefinition);
	}

	@Override
	public <R> RowsFetchSpec<R> map(
		Function<Row, R> mappingFunction) {
		return new DefaultSelectRowsFetchSpec<>(
			(o, o2) -> mappingFunction.apply(o));
	}

	@Override
	public <R> RowsFetchSpec<R> map(
		BiFunction<Row, RowMetadata, R> mappingFunction) {
		return new DefaultSelectRowsFetchSpec<>(mappingFunction);
	}

	@Override
	public RowsFetchSpec<Map<String, Object>> fetch() {
		return new DefaultSelectRowsFetchSpec<>(
			(row, rowMetadata) ->
				rowMetadata
					.getColumnNames()
					.stream()
					.collect(Collectors.toMap(Function.identity(), row::get))
		);
	}

	class DefaultSelectRowsFetchSpec<R> implements RowsFetchSpec<R> {

		DefaultSelectRowsFetchSpec(
			BiFunction<Row, RowMetadata, R> mappingFunction) {
			this.mappingFunction = mappingFunction;
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

		private Flux<R> _executeQuery() {

			if (query == null) {
				query = _createQuery();
			}

			if (_log.isDebugEnabled()) {
				_log.debug("executeQuery: " + query);
			}

			return DatabaseClientUtil.safeConnection(
				_connectionFactory, connection ->
					Flux.from(
						connection
							.createStatement(query)
							.execute())
						.flatMap(result -> result.map(mappingFunction))
			);

		}

		private String _createQuery() {

			Collection<String> columns = DefaultGenericSelectSpec.this._columns;

			Page page = DefaultGenericSelectSpec.this._page;

			Collection<Sort> sorts = DefaultGenericSelectSpec.this._sorts;

			StringBuilder sb = new StringBuilder("SELECT ");

			if (columns.isEmpty()) {
				sb.append(" * ");
			}
			else {
				sb.append(String.join(", ", columns)).append(' ');
			}

			sb
				.append(" FROM ")
				.append(DefaultGenericSelectSpec.this._table)
				.append(' ');

			CriteriaDefinition criteriaDefinition = DefaultGenericSelectSpec
				.this._criteriaDefinition;

			if (!criteriaDefinition.isEmpty()) {
				sb
					.append("WHERE ")
					.append(criteriaDefinition.toString())
					.append(' ');
			}

			boolean isNotDefaultPage = Page.DEFAULT != page;

			if (isNotDefaultPage) {

				Collection<Sort> pageOrder = page.getOrder();

				if (!pageOrder.isEmpty()) {
					sorts = pageOrder;
				}

			}

			if (!sorts.isEmpty()) {

				String sorting = sorts
					.stream()
					.map(sort -> {
						String column = sort.getColumn();

						switch (sort.getOrder()) {
							case ASC:
								column += " ASC";
								break;
							case DESC:
								column += " DESC";
								break;
						}

						return column;
					})
					.collect(Collectors.joining(", "));

				sb.append("ORDER BY ").append(sorting).append(' ');
			}

			if (isNotDefaultPage) {
				sb
					.append("LIMIT ").append(page.getSize()).append(' ')
					.append("OFFSET ").append(page.getSize() * page.getPage());

			}

			sb.append(';');

			return sb.toString();
		}

		private String query;

		private final BiFunction<Row, RowMetadata, R> mappingFunction;

	}

	private static final Logger _log = LoggerFactory.getLogger(
		DefaultGenericSelectSpec.class);


}
