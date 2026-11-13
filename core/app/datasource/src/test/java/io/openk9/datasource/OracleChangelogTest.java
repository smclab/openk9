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

import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.change.CheckSum;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Checks the SQL that the datasource changelog generates for Oracle 19c,
 * without a database: the same output as Liquibase {@code update-sql} on an
 * offline Oracle connection.
 */
class OracleChangelogTest {

	private static final String CHANGELOG = "db/datasource/changelog.xml";

	// the VARCHAR2 limit with MAX_STRING_SIZE=STANDARD, the Oracle default
	private static final int MAX_VARCHAR2_LENGTH = 4000;

	private static final Pattern VARCHAR2_LENGTH = Pattern.compile(
		"VARCHAR2\\s*\\(\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

	private static final Pattern BOOLEAN_DEFAULT = Pattern.compile(
		"DEFAULT\\s+(TRUE|FALSE)\\b", Pattern.CASE_INSENSITIVE);

	private static final String HIGHLIGHT_CHANGELOG =
		"db/datasource/2026/02/04-01-changelog.xml";

	private static final String HIGHLIGHT_CHANGESET_ID = "1770199022-1";

	// the checksum of the highlight changeset with a VARCHAR2(4096) description
	private static final String RELEASED_HIGHLIGHT_CHECKSUM =
		"9:f5a1616b5f7b798340b6b721a20c234e";

	@TempDir
	Path workDir;

	@Test
	void should_keep_oracle_varchar2_within_4000() throws Exception {
		// statements declaring a VARCHAR2 longer than the STANDARD limit
		List<String> tooLong = oracleStatements().stream()
			.filter(statement -> VARCHAR2_LENGTH.matcher(statement).results()
				.anyMatch(match ->
					Integer.parseInt(match.group(1)) > MAX_VARCHAR2_LENGTH))
			.toList();

		Assertions.assertEquals(List.of(), tooLong);
	}

	@Test
	void should_generate_numeric_boolean_defaults() throws Exception {
		// Oracle has no boolean literal: a TRUE or FALSE default is an
		// invalid identifier
		List<String> booleanDefaults = oracleStatements().stream()
			.filter(statement -> BOOLEAN_DEFAULT.matcher(statement).find())
			.toList();

		Assertions.assertEquals(List.of(), booleanDefaults);
	}

	@Test
	void should_accept_the_released_highlight_checksum() throws Exception {
		// the tenants that applied the highlight changeset as released keep
		// migrating after its description shrank to 4000
		try (Liquibase liquibase = offlineOracleLiquibase()) {
			ChangeSet changeSet = liquibase.getDatabaseChangeLog().getChangeSet(
				HIGHLIGHT_CHANGELOG, "openk9", HIGHLIGHT_CHANGESET_ID);

			Assertions.assertTrue(changeSet.isCheckSumValid(
				CheckSum.parse(RELEASED_HIGHLIGHT_CHECKSUM)));
		}
	}

	private List<String> oracleStatements() throws Exception {
		StringWriter sql = new StringWriter();

		try (Liquibase liquibase = offlineOracleLiquibase()) {
			liquibase.update(new Contexts(), new LabelExpression(), sql);
		}

		return Arrays.stream(sql.toString().split(";"))
			.map(String::strip)
			.filter(statement -> !statement.isEmpty())
			.toList();
	}

	private Liquibase offlineOracleLiquibase() throws Exception {
		ClassLoaderResourceAccessor accessor =
			new ClassLoaderResourceAccessor();

		// the offline connection tracks the applied changesets in a csv file;
		// outputLiquibaseSql=none leaves the DATABASECHANGELOG inserts out
		Database database = DatabaseFactory.getInstance().openDatabase(
			"offline:oracle?version=19.0&outputLiquibaseSql=none&changeLogFile="
				+ workDir.resolve("databasechangelog.csv"),
			null, null, null, accessor);

		return new Liquibase(CHANGELOG, accessor, database);
	}

}
