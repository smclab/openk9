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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.openk9.searcher.grpc.Field;
import io.openk9.searcher.payload.response.AutocompleteHit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;

public class AutocompleteTest {

	private static final String WEB_TITLE = "web.title";
	private final Map<String, List<String>> highlight1 = new HashMap<>();
	private final Map<String, List<String>> highlight2 = new HashMap<>();
	private final Map<String, List<String>> highlight3 = new HashMap<>();
	private SearchResource.AutocompleteContext defaultContext;

	@BeforeEach
	void setup(){

		highlight1.put(WEB_TITLE, List.of("Assicurazione"));
		highlight2.put(WEB_TITLE, List.of("assicurazione casa"));
		highlight3.put(WEB_TITLE, List.of("Riscatto polizza"));

		Field field = Field.newBuilder()
			.setFieldPath("web.title.searchasyoutype")
			.setParentPath(WEB_TITLE)
			.setLabel("Title")
			.setBoost(1D)
			.build();

		defaultContext = new SearchResource.AutocompleteContext(
			null,
			"assicurazione",
			false,
			List.of(field)
		);

	}

	@Test
	@DisplayName("Should remove autocomplete suggestions that match with the query text")
	void should_remove_autocomplete_matching_query_text() {

		// override context for this specific test
		var context = mock(SearchResource.AutocompleteContext.class);
		when(context.queryText()).thenReturn(defaultContext.queryText());
		when(context.perfectMatchIncluded()).thenReturn(false);
		when(context.fields()).thenReturn(defaultContext.fields());

		// Setup hit
		Hit<Map> hit = mock(Hit.class);
		when(hit.score()).thenReturn(1D);

		when(hit.highlight()).thenReturn(highlight1, highlight2, highlight3);

		// Setup hits
		HitsMetadata<Map> hitsMetadata = mock(HitsMetadata.class);
		when(hitsMetadata.hits()).thenReturn(List.of(hit, hit, hit));

		// Setup response
		SearchResponse<Map> response = mock(SearchResponse.class);
		when(response.hits()).thenReturn(hitsMetadata);

		// Test
		Set<AutocompleteHit> result =
			SearchResource._extractAutocompleteResponse(response, context);

		// Assertions
		assertEquals(2, result.size());
		var autocompleteTextList = result.stream()
			.map(AutocompleteHit::getAutocomplete)
			.map(String::toLowerCase)
			.toList();

		assertFalse(autocompleteTextList.contains(context.queryText().toLowerCase()));
	}

	@Test
	@DisplayName("Should keep autocomplete suggestions that match with the query text")
	void should_keep_autocomplete_matching_query_text() {

		// override context for this specific test
		var context = mock(SearchResource.AutocompleteContext.class);
		when(context.queryText()).thenReturn(defaultContext.queryText());
		when(context.perfectMatchIncluded()).thenReturn(true);
		when(context.fields()).thenReturn(defaultContext.fields());

		// Setup hit
		Hit<Map> hit = mock(Hit.class);
		when(hit.score()).thenReturn(1D);

		when(hit.highlight()).thenReturn(highlight1, highlight2, highlight3);

		// Setup hits
		HitsMetadata<Map> hitsMetadata = mock(HitsMetadata.class);
		when(hitsMetadata.hits()).thenReturn(List.of(hit, hit, hit));

		// Setup response
		SearchResponse<Map> response = mock(SearchResponse.class);
		when(response.hits()).thenReturn(hitsMetadata);

		// Test
		Set<AutocompleteHit> result =
			SearchResource._extractAutocompleteResponse(response, context);

		// Assertions
		assertEquals(3, result.size());
		var autocompleteTextList = result.stream()
			.map(AutocompleteHit::getAutocomplete)
			.map(String::toLowerCase)
			.toList();

		assertTrue(autocompleteTextList.contains(context.queryText().toLowerCase()));
	}
}
