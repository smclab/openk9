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

package io.openk9.searcher.resource;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResultObscurerTest {

	private static final JsonObject WEB_SOURCE = new JsonObject("""
		{
			"contentId": "c-1",
			"documentTypes": ["web"],
			"acl": { "public": false, "roles": ["finance"] },
			"web": {
				"title": "Progetto fusione",
				"url": "https://intranet.example/fusione",
				"content": {
					"base": "Piano della fusione",
					"i18n": { "it_IT": "Piano della fusione" }
				}
			}
		}
		""");

	@Test
	void should_keep_only_the_visible_fields_and_list_the_others() {
		// action
		var obscured = ResultObscurer.obscureSource(
			WEB_SOURCE.getMap(), Set.of("web.title"));

		// assertion — the obscured fields are the removed paths, sorted;
		// an i18n field is listed once, by its own path
		assertEquals(
			new JsonObject("""
				{
					"documentTypes": ["web"],
					"web": { "title": "Progetto fusione" },
					"obscuredFields": [
						"acl", "contentId", "web.content", "web.url"
					]
				}
				"""),
			new JsonObject(obscured));
	}

	@Test
	void should_keep_an_i18n_field_with_all_its_variants() {
		var obscured = ResultObscurer.obscureSource(
			WEB_SOURCE.getMap(), Set.of("web.content"));

		var web = (Map<?, ?>) obscured.get("web");

		assertEquals(
			WEB_SOURCE.getJsonObject("web").getJsonObject("content").getMap(),
			web.get("content"));
		assertEquals(
			List.of("acl", "contentId", "web.title", "web.url"),
			obscured.get(ResultObscurer.OBSCURED_FIELDS));
	}

	@Test
	void should_keep_the_visible_sub_fields_of_each_element_of_an_array() {
		// setup — two attachments, only their name is visible
		var source = new JsonObject("""
			{
				"documentTypes": ["document"],
				"resources": {
					"binaries": [
						{ "id": "b-1", "name": "budget.pdf", "data": "AAAA" },
						{ "id": "b-2", "name": "piano.pdf", "data": "BBBB" }
					]
				}
			}
			""");

		// action
		var obscured = ResultObscurer.obscureSource(
			source.getMap(), Set.of("resources.binaries.name"));

		// assertion — every element keeps its name, each removed path once
		assertEquals(
			new JsonObject("""
				{
					"documentTypes": ["document"],
					"resources": {
						"binaries": [
							{ "name": "budget.pdf" },
							{ "name": "piano.pdf" }
						]
					},
					"obscuredFields": [
						"resources.binaries.data", "resources.binaries.id"
					]
				}
				"""),
			new JsonObject(obscured));
	}

	@Test
	void should_drop_an_object_left_without_visible_values() {
		// setup — the visible title is missing from this document
		var source = new JsonObject("""
			{
				"documentTypes": ["web"],
				"web": { "url": "https://intranet.example/x" }
			}
			""");

		// action
		var obscured = ResultObscurer.obscureSource(
			source.getMap(), Set.of("web.title"));

		// assertion — no empty "web" object is left behind
		assertFalse(obscured.containsKey("web"));
		assertEquals(
			List.of("web.url"), obscured.get(ResultObscurer.OBSCURED_FIELDS));
	}

	@Test
	void should_mark_a_document_with_only_visible_fields_as_obscured() {
		// setup — a document holding only identifying and visible fields
		var source = new JsonObject("""
			{ "documentTypes": ["web"], "web": { "title": "Bilancio" } }
			""");

		// action
		var obscured = ResultObscurer.obscureSource(
			source.getMap(), Set.of("web.title"));

		// assertion — the empty list still marks the result as obscured
		assertEquals(List.of(), obscured.get(ResultObscurer.OBSCURED_FIELDS));
		assertEquals("Bilancio", ((Map<?, ?>) obscured.get("web")).get("title"));
	}

	@Test
	void should_drop_a_value_found_where_an_object_was_expected() {
		// setup — a visible sub-field, but the document holds a plain value
		var source = new JsonObject("""
			{ "documentTypes": ["web"], "meta": "raw text" }
			""");

		// action
		var obscured = ResultObscurer.obscureSource(
			source.getMap(), Set.of("meta.author"));

		// assertion
		assertFalse(obscured.containsKey("meta"));
		assertEquals(List.of("meta"), obscured.get(ResultObscurer.OBSCURED_FIELDS));
	}

	@Test
	void should_tell_the_visible_paths_of_a_highlight() {
		var visible = Set.of("web.title");

		// the field, its i18n variants and its multi-fields
		assertTrue(ResultObscurer.isVisible("web.title", visible));
		assertTrue(ResultObscurer.isVisible("web.title.i18n.it_IT", visible));
		assertTrue(ResultObscurer.isVisible("web.title.keyword", visible));

		// another field, also one whose name starts the same way
		assertFalse(ResultObscurer.isVisible("web.content", visible));
		assertFalse(ResultObscurer.isVisible("web.titles", visible));
	}

}
