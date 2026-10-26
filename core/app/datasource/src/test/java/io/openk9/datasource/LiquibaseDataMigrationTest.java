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

package io.openk9.datasource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import jakarta.inject.Inject;

import io.openk9.datasource.service.BucketService;

import io.agroal.api.AgroalDataSource;
import io.quarkus.liquibase.LiquibaseFactory;
import io.quarkus.test.junit.QuarkusTest;
import liquibase.Liquibase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@QuarkusTest
public class LiquibaseDataMigrationTest {

	private static final String DUPLICATE_NAME = "LiquibaseDataMigrationTest - dup";
	private static final String LONG_NAME = "L".repeat(255);

	@Inject
	BucketService bucketService;

	@Inject
	AgroalDataSource dataSource;

	@Inject
	LiquibaseFactory liquibaseFactory;

	// test for changeSet with id 1739804793-2
	@Test
	void should_return_sample_suggestion_from_sample_bucket() {
		var suggestionCategories = bucketService.findByName("public", "Sample Bucket")
			.flatMap(bucket ->
				bucketService.getSuggestionCategories(bucket.getId())
			)
			.await()
			.indefinitely();

		Assertions.assertEquals(1, suggestionCategories.size());

		var suggestionCategory = suggestionCategories.iterator().next();

		Assertions.assertEquals(
			"Sample Suggestion category", suggestionCategory.getName());
	}

	// test for changeSets with id 1793009947-1, 1793009947-2, 1793009947-3
	@ParameterizedTest
	@MethodSource("uniqueNameChangeSets")
	void should_rename_duplicate_names_before_adding_the_unique_constraint(
			String table, String changeSetId, String insertSql)
		throws Exception {

		try (Connection connection = dataSource.getConnection()) {
			// 1. Undo the changeset: drop its constraint and forget it ran
			execute(connection,
				"ALTER TABLE " + table + " DROP CONSTRAINT uk_" + table + "_name");
			execute(connection,
				"DELETE FROM databasechangelog WHERE id = '" + changeSetId + "'");

			try {
				// 2. Seed duplicates, ordered by id: a short name and a full-length one
				insert(connection, insertSql, -6, DUPLICATE_NAME);
				insert(connection, insertSql, -5, DUPLICATE_NAME);
				insert(connection, insertSql, -4, DUPLICATE_NAME);
				insert(connection, insertSql, -3, LONG_NAME);
				insert(connection, insertSql, -2, LONG_NAME);

				// 3. Rerun the migration
				try (Liquibase liquibase = liquibaseFactory.createLiquibase()) {
					liquibase.update(
						liquibaseFactory.createContexts(),
						liquibaseFactory.createLabels());
				}

				// the oldest keeps its name, the copies are numbered within 255 chars
				String longPrefix = LONG_NAME.substring(0, 255 - " - Copy 1".length());
				Assertions.assertEquals(
					List.of(
						DUPLICATE_NAME,
						DUPLICATE_NAME + " - Copy 1",
						DUPLICATE_NAME + " - Copy 2",
						LONG_NAME,
						longPrefix + " - Copy 1"),
					seededNames(connection, table));

				// the constraint is back: a new duplicate is rejected
				Assertions.assertThrows(SQLException.class, () ->
					insert(connection, insertSql, -1, DUPLICATE_NAME));
			}
			finally {
				// 4. Clean up the seeded rows
				execute(connection, "DELETE FROM " + table + " WHERE id < 0");
			}
		}
	}

	private static Stream<Arguments> uniqueNameChangeSets() {
		return Stream.of(
			Arguments.of(
				"sorting", "1793009947-1",
				"INSERT INTO sorting (id, name, priority, default_sort, type) "
					+ "VALUES (?, ?, 1, false, 'ASC')"),
			Arguments.of(
				"autocomplete", "1793009947-2",
				"INSERT INTO autocomplete (id, name) VALUES (?, ?)"),
			Arguments.of(
				"autocorrection", "1793009947-3",
				"INSERT INTO autocorrection (id, name, sort, suggest_mode, "
					+ "autocorrection_doc_type_field_id) "
					+ "VALUES (?, ?, 'SCORE', 'MISSING', "
					+ "(SELECT MIN(id) FROM doc_type_field))"));
	}

	private static void execute(Connection connection, String sql)
		throws SQLException {

		try (Statement statement = connection.createStatement()) {
			statement.execute(sql);
		}
	}

	private static void insert(
			Connection connection, String insertSql, long id, String name)
		throws SQLException {

		try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
			statement.setLong(1, id);
			statement.setString(2, name);
			statement.executeUpdate();
		}
	}

	private static List<String> seededNames(Connection connection, String table)
		throws SQLException {

		List<String> names = new ArrayList<>();
		try (Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery(
				 "SELECT name FROM " + table + " WHERE id < 0 ORDER BY id")) {

			while (resultSet.next()) {
				names.add(resultSet.getString(1));
			}
		}
		return names;
	}

}
