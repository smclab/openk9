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

package io.openk9.datasource.config.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Pure unit test (no Quarkus, no Docker) of the {@link SchemaVersion} parser and
 * of compatibility policy B: same major, incoming minor no newer than current.
 */
class SchemaVersionTest {

	@Test
	void parse_rejects_null_blank_and_malformed() {
		assertTrue(SchemaVersion.parse(null).isEmpty());
		assertTrue(SchemaVersion.parse("").isEmpty());
		assertTrue(SchemaVersion.parse("1").isEmpty());
		assertTrue(SchemaVersion.parse("1.0.0").isEmpty());
		assertTrue(SchemaVersion.parse("1.x").isEmpty());
		assertTrue(SchemaVersion.parse("v1.0").isEmpty());
	}

	@Test
	void parse_reads_major_and_minor_trimming_whitespace() {
		SchemaVersion version = SchemaVersion.parse(" 2.7 ").orElseThrow();
		assertEquals(2, version.major());
		assertEquals(7, version.minor());
	}

	@Test
	void accepts_the_same_version() {
		assertTrue(new SchemaVersion(1, 2).accepts(new SchemaVersion(1, 2)));
	}

	@Test
	void accepts_an_older_minor_of_the_same_major() {
		assertTrue(new SchemaVersion(1, 2).accepts(new SchemaVersion(1, 0)));
	}

	@Test
	void rejects_a_newer_minor_of_the_same_major() {
		assertFalse(new SchemaVersion(1, 0).accepts(new SchemaVersion(1, 9)));
	}

	@Test
	void rejects_a_different_major() {
		assertFalse(new SchemaVersion(1, 0).accepts(new SchemaVersion(2, 0)));
		assertFalse(new SchemaVersion(1, 0).accepts(new SchemaVersion(0, 9)));
	}

	@Test
	void current_is_parseable_and_accepts_itself() {
		assertTrue(SchemaVersion.CURRENT.accepts(SchemaVersion.CURRENT));
	}

}
