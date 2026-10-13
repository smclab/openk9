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

package io.openk9.searcher.client.mapper;

import io.openk9.searcher.client.dto.SearchRequest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearcherMapperTest {

	private final SearcherMapper mapper = new SearcherMapperImpl();

	@Test
	void should_carry_include_obscured_to_the_query_parser() {
		// a request asking for the obscured results
		var searchRequest = new SearchRequest();
		searchRequest.setIncludeObscured(true);

		var queryParserRequest = mapper.toQueryParserRequest(searchRequest);

		assertTrue(queryParserRequest.getIncludeObscured());
	}

	@Test
	void should_not_ask_for_obscured_results_by_default() {
		// a request that does not set the flag, as every existing client
		var queryParserRequest = mapper.toQueryParserRequest(new SearchRequest());

		assertFalse(queryParserRequest.getIncludeObscured());
	}

}
