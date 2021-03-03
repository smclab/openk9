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

package com.openk9.schemaregistry.internal.repository;

import com.openk9.schemaregistry.model.Schema;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Statement;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class QueryUtil {

	public static Mono<Schema> addSchema(
		Connection connection, Schema schema) {

		return Mono.from(
			connection
				.createStatement(_INSERT_INTO_SCHEMA_REPOSITORY)
				.bind("$1", schema.getVersion())
				.bind("$2", schema.getSubject())
				.bind("$3", schema.getFormat())
				.bind("$4", schema.getDefinition())
				.execute()
		).flatMap(result -> Mono.from(result.map(Schema::mapping)))
			.doFinally(signalType -> Mono.from(connection.close()).subscribe());
	}

	public static Mono<?> removeSchema(
		Connection connection, Mono<Schema> mono) {

		return mono.map(Schema::getId)
			.flatMap(id -> Mono.from(
				connection
					.createStatement(_DELETE_ENTITY)
					.bind("$1", id)
					.execute()
				)
			).doFinally(signalType -> Mono.from(connection.close()).subscribe());

	}


	public static Flux<Schema> addSchemas(
		Connection connection, List<Schema> schemas) {

		Statement statement =
			connection.createStatement(_INSERT_INTO_SCHEMA_REPOSITORY);

		for (Schema schema : schemas) {
			statement.bind("$1", schema.getVersion())
				.bind("$2", schema.getSubject())
				.bind("$3", schema.getFormat())
				.bind("$4", schema.getDefinition())
				.add();
		}

		return Flux
			.from(statement.execute())
			.flatMap(result -> result.map(Schema::mapping))
			.doFinally(signalType -> Mono.from(connection.close()).subscribe());

	}

	public static Mono<Schema> findById(Connection connection, Integer id) {

		return Mono.from(
			connection
				.createStatement(_FIND_BY_ID)
				.bind("$1", id)
				.execute())
			.flatMap(result -> Mono.from(result.map(Schema::mapping)))
			.doFinally(signalType -> Mono.from(connection.close()).subscribe());

	}

	public static Mono<Schema> findBySubjectAndFormatAndVersion(
		Connection connection, String subject, String format, Integer version) {

		return Mono.from(
			connection
				.createStatement(_FIND_BY_SUBJECT_AND_FORMAT_AND_VERSION)
				.bind("$1", subject)
				.bind("$2", format)
				.bind("$3", version)
				.execute())
			.flatMap(result -> Mono.from(result.map(Schema::mapping)))
			.doFinally(signalType -> Mono.from(connection.close()).subscribe());
	}

	public static Flux<Schema> findBySubjectAndFormatOrderByVersion(
		Connection connection, String subject, String format) {

		return Flux.from(
			connection
				.createStatement(_FIND_BY_SUBJECT_AND_FORMAT_ORDER_BY_VERSION)
				.bind("$1", subject)
				.bind("$2", format)
				.execute())
			.flatMap(result -> result.map(Schema::mapping))
			.doFinally(signalType -> Mono.from(connection.close()).subscribe());
	}

	private static String allColumnAlias(String alias) {
		return Arrays
			.stream(ALL_COLUMN)
			.collect(Collectors.joining(
				",", alias + ".", ""));
	}

	private static String allColumn() {
		return String.join(",", ALL_COLUMN);
	}

	private static String allColumn(int fromIndex) {
		return String.join(
			",", Arrays.copyOfRange(ALL_COLUMN, fromIndex, ALL_COLUMN.length));
	}

	private static String updateColumns(int fromIndex) {

		return IntStream
			.rangeClosed(fromIndex, ALL_COLUMN.length)
			.mapToObj(i -> ALL_COLUMN[i - 1] + "=" + "$" + i)
			.collect(Collectors.joining(","));

	}

	private static String placeHolders() {
		return placeHolders(ALL_COLUMN);
	}

	private static String placeHolders(int fromIndex) {
		return placeHolders(
			Arrays.copyOfRange(ALL_COLUMN, fromIndex, ALL_COLUMN.length));
	}

	private static String placeHolders(String[] columns) {
		return IntStream
			.rangeClosed(1, columns.length)
			.boxed()
			.map(Object::toString)
			.map(e -> "$" + e)
			.collect(Collectors.joining(","));
	}

	private static String _generateFindBy(String...columns) {
		StringBuilder sb = new StringBuilder(_FIND_ALL);

		sb.append(' ').append("WHERE").append(' ');

		String tmp = IntStream
			.rangeClosed(1, columns.length)
			.mapToObj(i -> "t1." + columns[i - 1] + " = " + "$" + i )
			.collect(Collectors.joining(" AND "));

		return sb.append(tmp).toString();

	}


	public static final String TABLE_NAME = "SCHEMA_REPOSITORY";

	public static final String[] ALL_COLUMN =
		{"id", "version", "subject", "format", "definition"};

	private static final String _FIND_ALL =
		"SELECT " + allColumnAlias("t1") + " " +
		"FROM " + TABLE_NAME + " AS t1 ";

	private static final String _FIND_BY_SUBJECT_AND_FORMAT_ORDER_BY_VERSION =
		_FIND_ALL +
		"WHERE t1.subject = $1 AND t1.format = $2 " +
		"ORDER BY t1.version";

	private static final String _FIND_BY_SUBJECT_AND_FORMAT_AND_VERSION =
		_FIND_ALL +
		"WHERE t1.subject = $1 AND t1.format = $2 AND  t1.version = $3";

	private static final String _FIND_BY_ID =
		_FIND_ALL +
		"WHERE t1.id = $1";

	private static final String _INSERT_INTO_SCHEMA_REPOSITORY =
		"INSERT INTO " + TABLE_NAME + " (" + allColumn(1) + ") VALUES (" + placeHolders(1) + ")";

	private static final String _UPDATE_INTO_SCHEMA_REPOSITORY =
		"UPDATE " + TABLE_NAME + " SET " + updateColumns(2) + " WHERE id = $1";

	private static final String _DELETE_ENTITY =
		"DELETE FROM " + TABLE_NAME + " WHERE t1.id = $1";

}
