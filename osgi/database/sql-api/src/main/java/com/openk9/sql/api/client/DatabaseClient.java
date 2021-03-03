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

package com.openk9.sql.api.client;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface DatabaseClient {

	SelectFromSpec select();
	InsertFromSpec insert();
	DeleteFromSpec delete();
	UpdateFromSpec update();

	interface SelectSpec<S extends SelectSpec<S>> {

		S orderBy(Sort...sort);

		S orderBy(Sort sort);

		S page(Page page);

		S project(String column);

		S project(String...columns);

		S project(Collection<String> columns);

		S matching(CriteriaDefinition criteria);

	}

	interface SelectFromSpec {

		GenericSelectSpec from(String table);

	}

	interface GenericSelectSpec extends SelectSpec<GenericSelectSpec> {

		<R> RowsFetchSpec<R> map(Function<Row, R> mappingFunction);

		<R> RowsFetchSpec<R> map(BiFunction<Row, RowMetadata, R> mappingFunction);

		RowsFetchSpec<Map<String, Object>> fetch();
	}

	interface TypedSelectSpec<T> extends SelectSpec<TypedSelectSpec<T>> {

		<R> RowsFetchSpec<R> map(Function<Row, R> mappingFunction);

		<R> RowsFetchSpec<R> map(BiFunction<Row, RowMetadata, R> mappingFunction);

		FetchSpec<T> fetch();
	}

	interface InsertFromSpec {
		GenericInsertSpec<Map<String, Object>> into(String table);
	}

	interface InsertSpec<T> {

		<R> RowsFetchSpec<R> map(Function<Row, R> mappingFunction);

		<R> RowsFetchSpec<R> map(BiFunction<Row, RowMetadata, R> mappingFunction);

		FetchSpec<T> fetch();

		Mono<Void> then();

	}

	interface GenericInsertSpec<T> extends InsertSpec<T> {
		GenericInsertSpec<T> value(String field, Object value);
		GenericInsertSpec<T> value(Map<String, Object> fieldValues);
	}

	interface DeleteSpec extends UpdatedRowsFetchSpec {

		Mono<Void> then();

		<R> RowsFetchSpec<R> map(Function<Row, R> mappingFunction);

		<R> RowsFetchSpec<R> map(
			BiFunction<Row, RowMetadata, R> mappingFunction);

		<R> RowsFetchSpec<R> map(Class<R> clazz);
	}


	interface DeleteMatchingSpec {
		DeleteSpec matching(CriteriaDefinition criteria);
	}


	interface DeleteFromSpec {
		DeleteMatchingSpec from(String table);
	}

	interface UpdateSpec<T> {
		<R> RowsFetchSpec<R> map(Function<Row, R> mappingFunction);

		<R> RowsFetchSpec<R> map(
			BiFunction<Row, RowMetadata, R> mappingFunction);

		FetchSpec<Map<String, Object>> fetch();

		Mono<Void> then();
	}

	interface GenericUpdateSpec<T> extends UpdateSpec<T> {

		GenericUpdateSpec<T> value(String field, Object value);

		GenericUpdateSpec<T> value(Map<String, Object> fieldValues);

		UpdateSpec<T> matching(CriteriaDefinition criteria);
	}

	interface UpdateFromSpec {
		GenericUpdateSpec<Map<String, Object>> from(String table);
	}

}
