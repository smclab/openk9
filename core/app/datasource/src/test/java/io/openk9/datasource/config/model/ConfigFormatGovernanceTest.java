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

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Governance tripwire (pure unit, always-on): the set of exportable
 * {@link ConfigEntityType} constants is the wire format of a
 * {@link ConfigPackage}, so it is pinned here and tied to
 * {@link ConfigPackage#CURRENT_SCHEMA_VERSION}. A deliberate, fully wired format
 * change makes this the single failing test, forcing a conscious version bump.
 * It is orthogonal to the metamodel/mapper/natural-key guards, which fire only
 * on incomplete wiring.
 */
class ConfigFormatGovernanceTest {

	private static final String EXPECTED_SCHEMA_VERSION = "1.0";

	private static final Set<String> EXPECTED_TYPES = Set.of(
		"BUCKET",
		"DATASOURCE",
		"PLUGIN_DRIVER",
		"ENRICH_PIPELINE",
		"ENRICH_PIPELINE_ITEM",
		"ENRICH_ITEM",
		"DOC_TYPE",
		"DOC_TYPE_FIELD",
		"DOC_TYPE_TEMPLATE",
		"ANALYZER",
		"CHAR_FILTER",
		"TOKEN_FILTER",
		"TOKENIZER",
		"ACL_MAPPING",
		"QUERY_ANALYSIS",
		"ANNOTATOR",
		"RULE",
		"QUERY_PARSER_CONFIG",
		"SEARCH_CONFIG",
		"EMBEDDING_MODEL",
		"LARGE_LANGUAGE_MODEL",
		"LANGUAGE",
		"RAG_CONFIGURATION",
		"SUGGESTION_CATEGORY",
		"TAB",
		"SORTING",
		"TOKEN_TAB",
		"AUTOCOMPLETE",
		"AUTOCORRECTION",
		"HIGHLIGHT");

	@Test
	void exportable_type_set_is_pinned_to_the_schema_version() {
		Set<String> actual = Arrays.stream(ConfigEntityType.values())
			.map(Enum::name)
			.collect(Collectors.toSet());

		assertEquals(EXPECTED_TYPES, actual,
			"The set of exportable ConfigEntityType constants changed. If this is a "
			+ "wire-format change, bump ConfigPackage.CURRENT_SCHEMA_VERSION (MAJOR for a "
			+ "removal/rename, MINOR for an addition), then update EXPECTED_TYPES and "
			+ "EXPECTED_SCHEMA_VERSION in this test.");

		assertEquals(EXPECTED_SCHEMA_VERSION, ConfigPackage.CURRENT_SCHEMA_VERSION,
			"CURRENT_SCHEMA_VERSION changed: update EXPECTED_SCHEMA_VERSION here and confirm "
			+ "the exportable type set above is still correct for the new version.");
	}

}
