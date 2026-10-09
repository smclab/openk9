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

package io.openk9.datasource.searcher;

import java.util.List;
import java.util.stream.Stream;

import io.openk9.datasource.model.SearchConfig;
import io.openk9.searcher.grpc.SearchTokenRequest;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opensearch.search.builder.SearchSourceBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SearcherServiceMinScoreTest {

	private static final SearchTokenRequest TEXT_TOKEN =
		SearchTokenRequest.newBuilder()
			.setTokenType("TEXT")
			.addValues("test")
			.setFilter(false)
			.build();

	private static final SearchTokenRequest FILTER_TOKEN =
		SearchTokenRequest.newBuilder()
			.setTokenType("TEXT")
			.addValues("test")
			.setFilter(true)
			.build();

	@ParameterizedTest(name = "{0}")
	@MethodSource("cases")
	void should_apply_min_score(
		String name, List<SearchTokenRequest> searchQuery,
		SearchConfig searchConfig, Float expected) {

		var searchSourceBuilder = new SearchSourceBuilder();

		SearcherService.applyMinScore(
			searchSourceBuilder, searchQuery, searchConfig);

		assertEquals(expected, searchSourceBuilder.minScore());
	}

	private static Stream<Arguments> cases() {
		return Stream.of(
			Arguments.of(
				"search off, text token", List.of(TEXT_TOKEN),
				searchConfig(10f, false), 0.5f),
			Arguments.of(
				"search on, text token", List.of(TEXT_TOKEN),
				searchConfig(10f, true), 10f),
			Arguments.of(
				"search on, empty query", List.of(),
				searchConfig(10f, true), 10f),
			Arguments.of(
				"search on, filter only", List.of(FILTER_TOKEN),
				searchConfig(10f, true), 10f),
			Arguments.of(
				"search off, empty query", List.of(),
				searchConfig(10f, false), null),
			Arguments.of(
				"search off, filter only", List.of(FILTER_TOKEN),
				searchConfig(10f, false), null),
			Arguments.of(
				"no search config, text token", List.of(TEXT_TOKEN),
				null, 0.5f),
			Arguments.of(
				"default search config, text token", List.of(TEXT_TOKEN),
				searchConfig(0f, false), 0.5f)
		);
	}

	private static SearchConfig searchConfig(
		float minScore, boolean minScoreSearch) {

		var searchConfig = new SearchConfig();
		searchConfig.setMinScore(minScore);
		searchConfig.setMinScoreSearch(minScoreSearch);
		return searchConfig;
	}

}
