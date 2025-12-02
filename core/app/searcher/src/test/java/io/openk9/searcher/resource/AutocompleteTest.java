/*
 * Copyright (C) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
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

package io.openk9.searcher.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AutocompleteTest {

	private Map<String, Object> testMap;

	@BeforeEach
	void setUp() {
		testMap = new HashMap<>();

		Map<String, Object> webMap = new HashMap<>();
		Map<String, Object> titleMap = new HashMap<>();
		Map<String, Object> metaMap = new HashMap<>();

		titleMap.put("h1", "Main Title");
		titleMap.put("h2", "Subtitle");
		titleMap.put("h3", "Sub-subtitle");

		metaMap.put("author", "John Doe");
		metaMap.put("date", "2024-01-01");

		webMap.put("title", titleMap);
		webMap.put("url", "https://example.com");
		webMap.put("meta", metaMap);

		testMap.put("web", webMap);
		testMap.put("version", "1.0");
	}

	@Test
	void should_get_top_level_value() {
		Object result = SearchResource._getNestedValue(testMap, "version");
		assertEquals("1.0", result);
	}

	@Test
	void should_get_nested_map() {
		Object result = SearchResource._getNestedValue(testMap, "web");
		assertNotNull(result);
		assertInstanceOf(Map.class, result);
	}

	@Test
	void should_get_two_level_value() {
		Object result = SearchResource._getNestedValue(testMap, "web.url");
		assertEquals("https://example.com", result);
	}

	@Test
	void should_get_three_level_value() {
		Object result = SearchResource._getNestedValue(testMap, "web.title.h2");
		assertEquals("Subtitle", result);
	}

	@Test
	void should_get_null_on_missing_key() {
		Object result = SearchResource._getNestedValue(testMap, "web.title.h4");
		assertNull(result);
	}

	@Test
	void should_get_null_on_missing_top_level_key() {
		Object result = SearchResource._getNestedValue(testMap, "api");
		assertNull(result);
	}

	@Test
	void should_get_null_on_null_path() {
		Object result = SearchResource._getNestedValue(testMap, null);
		assertNull(result);
	}

	@Test
	void should_get_null_on_empty_path() {
		Object result = SearchResource._getNestedValue(testMap, "");
		assertNull(result);
	}
}
