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

import io.openk9.searcher.client.dto.SearchRequest;
import io.openk9.searcher.grpc.ResultAcl;

import org.junit.jupiter.api.Test;
import org.opensearch.core.common.bytes.BytesArray;
import org.opensearch.core.common.text.Text;
import org.opensearch.search.DocValueFormat;
import org.opensearch.search.SearchHit;
import org.opensearch.search.fetch.subphase.highlight.HighlightField;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchResourceResultAclTest {

	private static final String AUTHORIZED = "k9_acl_authorized";

	private static final ResultAcl RESULT_ACL = ResultAcl.newBuilder()
		.setAuthorizedQueryName(AUTHORIZED)
		.addVisibleFields("web.title")
		.build();

	@Test
	void should_obscure_a_result_the_user_is_not_authorized_to_read() {
		// setup — the hit did not match the named query of the ACL
		var hit = webHit();

		// action
		var result = SearchResource.toHitMap(hit, RESULT_ACL, true, true);

		// assertion — only the visible field, the listed ones and the
		// highlight on the visible field
		var source = source(result);
		assertEquals(List.of("web"), source.get("documentTypes"));
		assertEquals(Map.of("title", "Progetto fusione"), source.get("web"));
		assertEquals(
			List.of("contentId", "web.content", "web.url"),
			source.get(ResultObscurer.OBSCURED_FIELDS));
		assertEquals("doc-1", source.get("id"));
		assertEquals(Set.of("web.title"), highlight(result).keySet());
		assertFalse(result.containsKey("sortAfterKey"));
	}

	@Test
	void should_leave_an_authorized_result_whole() {
		// setup — the hit matched the named query of the ACL
		var hit = webHit();
		hit.matchedQueries(new String[] {AUTHORIZED});

		// action
		var result = SearchResource.toHitMap(hit, RESULT_ACL, true, true);

		// assertion — the whole source and highlight, still no sortAfterKey
		var source = source(result);
		assertFalse(source.containsKey(ResultObscurer.OBSCURED_FIELDS));
		assertEquals("c-1", source.get("contentId"));
		assertEquals(2, highlight(result).size());
		assertFalse(result.containsKey("sortAfterKey"));
	}

	@Test
	void should_keep_the_sort_after_key_outside_the_obscured_mode() {
		// action — a sorted search that did not ask for the obscured results
		var result = SearchResource.toHitMap(webHit(), null, false, true);

		// assertion — as before the mode existed
		var source = source(result);
		assertFalse(source.containsKey(ResultObscurer.OBSCURED_FIELDS));
		assertEquals("c-1", source.get("contentId"));
		assertTrue(result.containsKey("sortAfterKey"));
	}

	@Test
	void should_drop_the_sort_after_key_whenever_the_request_asks_for_it() {
		// action — the mode asked by the request, but nothing to obscure:
		// a super-user, or a tenant with the mode disabled
		var result = SearchResource.toHitMap(webHit(), null, true, true);

		// assertion
		assertFalse(source(result).containsKey(ResultObscurer.OBSCURED_FIELDS));
		assertFalse(result.containsKey("sortAfterKey"));
	}

	@Test
	void should_leave_out_the_sort_after_key_without_a_user_sort() {
		// action — outside the mode, but the hit is sorted only by the
		// tie-break, not by a user sort
		var result = SearchResource.toHitMap(webHit(), null, false, false);

		// assertion
		assertFalse(result.containsKey("sortAfterKey"));
	}

	@Test
	void should_ignore_the_sort_after_key_received_in_the_obscured_mode() {
		var searchRequest = new SearchRequest();
		searchRequest.setSortAfterKey("WzcwMDBd");

		// outside the mode the key reaches the datasource
		assertEquals("WzcwMDBd", SearchResource.sortAfterKeyOf(searchRequest));

		// in the mode it is ignored, as if the client sent none
		searchRequest.setIncludeObscured(true);
		assertEquals("", SearchResource.sortAfterKeyOf(searchRequest));

		// no key at all
		assertEquals("", SearchResource.sortAfterKeyOf(new SearchRequest()));
	}

	private static SearchHit webHit() {
		var hit = new SearchHit(1, "doc-1", Map.of(), Map.of());

		hit.sourceRef(new BytesArray("""
			{
				"contentId": "c-1",
				"documentTypes": ["web"],
				"web": {
					"title": "Progetto fusione",
					"url": "https://intranet.example/fusione",
					"content": "Piano della fusione"
				}
			}
			"""));
		hit.score(3.2f);
		hit.sortValues(
			new Object[] {"Progetto fusione"},
			new DocValueFormat[] {DocValueFormat.RAW});
		hit.highlightFields(Map.of(
			"web.title", new HighlightField(
				"web.title", new Text[] {new Text("Progetto <em>fusione</em>")}),
			"web.content", new HighlightField(
				"web.content", new Text[] {new Text("Piano della <em>fusione</em>")})
		));

		return hit;
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> source(Map<String, Object> result) {
		return (Map<String, Object>) result.get("source");
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> highlight(Map<String, Object> result) {
		return (Map<String, Object>) result.get("highlight");
	}

}
