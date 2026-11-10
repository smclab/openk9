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

import jakarta.inject.Inject;

import io.openk9.datasource.service.BucketService;

import io.agroal.api.AgroalDataSource;
import io.quarkus.liquibase.LiquibaseFactory;
import io.quarkus.test.junit.QuarkusTest;
import liquibase.Liquibase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class LiquibaseDataMigrationTest {

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

	// test for changeSets with id 1794300000-1, 1794300000-2
	@Test
	void should_add_the_base_path_to_web_crawler_drivers_registered_at_the_root()
		throws Exception {

		String root = "openk9-web-connector:5000";

		try (Connection connection = dataSource.getConnection()) {
			// 1. Forget the changesets
			execute(connection,
				"DELETE FROM databasechangelog "
					+ "WHERE id IN ('1794300000-1', '1794300000-2')");

			try {
				// 2. Seed drivers, ordered by id
				insertDriver(connection, -9, root, "/startUrlsCrawling");
				insertDriver(connection, -8, root + "/", "/startUrlsCrawling");
				insertDriver(connection, -7, root, "/startSitemapCrawling");
				insertDriver(connection, -6, root + "/urls", "/startUrlsCrawling");
				insertDriver(connection, -5, root + "/sitemap", "/startUrlsCrawling");
				insertDriver(connection, -4, root + "/sitemap", "/startSitemapCrawling");
				insertDriver(connection, -3, "openk9-s3-connector:5000", "/execute");

				// 3. Rerun the migration
				try (Liquibase liquibase = liquibaseFactory.createLiquibase()) {
					liquibase.update(
						liquibaseFactory.createContexts(),
						liquibaseFactory.createLabels());
				}

				// only the drivers at the root get the base path, the path is unchanged
				Assertions.assertEquals(
					List.of(
						root + "/urls|/startUrlsCrawling",
						root + "/urls|/startUrlsCrawling",
						root + "/sitemap|/startSitemapCrawling",
						root + "/urls|/startUrlsCrawling",
						root + "/sitemap|/startUrlsCrawling",
						root + "/sitemap|/startSitemapCrawling",
						"openk9-s3-connector:5000|/execute"),
					seededDrivers(connection));
			}
			finally {
				// 4. Clean up the seeded rows
				execute(connection, "DELETE FROM plugin_driver WHERE id < 0");
			}
		}
	}

	private static void execute(Connection connection, String sql)
		throws SQLException {

		try (Statement statement = connection.createStatement()) {
			statement.execute(sql);
		}
	}

	private static void insertDriver(
			Connection connection, long id, String baseUri, String path)
		throws SQLException {

		String jsonConfig = "{\"secure\":false,\"baseUri\":\"" + baseUri
			+ "\",\"path\":\"" + path + "\",\"method\":\"POST\"}";

		try (PreparedStatement statement = connection.prepareStatement(
			"INSERT INTO plugin_driver (id, name, type, provisioning, json_config) "
				+ "VALUES (?, ?, 'HTTP', 'USER', ?)")) {

			statement.setLong(1, id);
			statement.setString(2, "LiquibaseDataMigrationTest - driver " + id);
			statement.setString(3, jsonConfig);
			statement.executeUpdate();
		}
	}

	private static List<String> seededDrivers(Connection connection)
		throws SQLException {

		List<String> drivers = new ArrayList<>();
		try (Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery(
				 "SELECT json_config::json->>'baseUri', json_config::json->>'path' "
					 + "FROM plugin_driver WHERE id < 0 ORDER BY id")) {

			while (resultSet.next()) {
				drivers.add(resultSet.getString(1) + "|" + resultSet.getString(2));
			}
		}
		return drivers;
	}

}
