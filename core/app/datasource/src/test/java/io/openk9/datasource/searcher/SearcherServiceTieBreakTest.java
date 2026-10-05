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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import io.openk9.searcher.grpc.Sort;
import org.junit.jupiter.api.Test;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.FieldSortBuilder;
import org.opensearch.search.sort.ScoreSortBuilder;
import org.opensearch.search.sort.SortBuilder;
import org.opensearch.search.sort.SortBuilders;
import org.opensearch.search.sort.SortOrder;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearcherServiceTieBreakTest {

	private static final String CONTENT_ID = "contentId.keyword";
	private static final String NUMBER = "number";
	private static final boolean HYBRID = true;
	private static final boolean NOT_HYBRID = false;
	private static final boolean CHUNKED = true;
	private static final boolean NOT_CHUNKED = false;
	private static final List<Sort> USER_SORT = List.of(
		Sort.newBuilder().setField("web.content").build());

	@Test
	void should_sort_by_score_then_tie_break_when_no_sort_was_requested() {
		var searchSourceBuilder = new SearchSourceBuilder();

		SearcherService.applyTieBreak(searchSourceBuilder, NOT_HYBRID, CHUNKED);

		// writing a sort clause switches that implicit order off, so _score
		// has to become the first criterion for relevance to survive
		var sorts = searchSourceBuilder.sorts();
		assertEquals(3, sorts.size());

		var scoreSort = assertInstanceOf(ScoreSortBuilder.class, sorts.getFirst());
		assertEquals(SortOrder.DESC, scoreSort.order());

		// the two tie-break criteria follow, in this order: documents sharing
		// a contentId are chunks of the same content and differ by number
		assertEquals(List.of(CONTENT_ID, NUMBER), fieldSortNames(sorts));
	}

	@Test
	void should_close_the_requested_sorts_with_the_tie_break() {
		var searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.sort(SortBuilders
			.fieldSort("lastModifiedDate")
			.order(SortOrder.DESC));
		searchSourceBuilder.sort(SortBuilders
			.fieldSort("title.keyword")
			.order(SortOrder.ASC));

		SearcherService.applyTieBreak(searchSourceBuilder, NOT_HYBRID, CHUNKED);

		// the criteria are evaluated in sequence, so the tie-break only breaks
		// ties the requested ones left open: it has to stay last
		var sorts = searchSourceBuilder.sorts();
		assertEquals(
			List.of("lastModifiedDate", "title.keyword", CONTENT_ID, NUMBER),
			fieldSortNames(sorts)
		);

		// no _score here: the request already states how to order, and adding
		// relevance to the list would change which documents come first
		assertTrue(sorts.stream().noneMatch(ScoreSortBuilder.class::isInstance));
	}

	@Test
	void should_search_indices_that_do_not_map_the_tie_break_fields() {
		// the tie-break is appended to every search, including the ones that
		// run on indices without chunks
		var searchSourceBuilder = new SearchSourceBuilder();

		SearcherService.applyTieBreak(searchSourceBuilder, NOT_HYBRID, CHUNKED);

		// without unmappedType the search fails with "No mapping found for
		// [number]" instead of ignoring the criterion
		var contentIdSort = findSortOnField(searchSourceBuilder, CONTENT_ID);
		assertEquals("keyword", contentIdSort.unmappedType());
		assertEquals(SortOrder.ASC, contentIdSort.order());

		var numberSort = findSortOnField(searchSourceBuilder, NUMBER);
		assertEquals("integer", numberSort.unmappedType());
		assertEquals(SortOrder.ASC, numberSort.order());

		// a mapped index can still hold documents without number: they belong
		// after the chunks, not before them
		assertEquals("_last", numberSort.missing());
	}

	@Test
	void should_leave_a_hybrid_query_unsorted_to_keep_its_relevance_order() {
		var searchSourceBuilder = new SearchSourceBuilder();

		SearcherService.applyTieBreak(searchSourceBuilder, HYBRID, CHUNKED);

		// OpenSearch rejects _score next to any other criterion on a hybrid
		// query, because the final score only exists once the normalization
		// processor has combined the sub-queries; sorting by the tie-break
		// alone would rank by contentId and throw relevance away
		assertNull(searchSourceBuilder.sorts());
	}

	@Test
	void should_close_the_requested_sorts_of_a_hybrid_query_too() {
		var searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.sort(SortBuilders
			.fieldSort("web.title.keyword")
			.order(SortOrder.ASC));

		SearcherService.applyTieBreak(searchSourceBuilder, HYBRID, CHUNKED);

		// no _score is involved, so the hybrid query accepts the criteria and
		// its ties become deterministic like everywhere else
		var sorts = searchSourceBuilder.sorts();
		assertEquals(
			List.of("web.title.keyword", CONTENT_ID, NUMBER),
			fieldSortNames(sorts)
		);
		assertTrue(sorts.stream().noneMatch(ScoreSortBuilder.class::isInstance));
	}

	@Test
	void should_close_with_the_content_id_alone_when_no_index_is_chunked() {
		var searchSourceBuilder = new SearchSourceBuilder();

		SearcherService.applyTieBreak(searchSourceBuilder, NOT_HYBRID, NOT_CHUNKED);

		// number is not ours outside chunked indices: an index that maps it as
		// text would fail the sort and lose all its documents from the results
		assertEquals(List.of(CONTENT_ID), fieldSortNames(searchSourceBuilder.sorts()));
	}

	@Test
	void should_not_stack_criteria_when_applied_twice() {
		var searchSourceBuilder = new SearchSourceBuilder();

		SearcherService.applyTieBreak(searchSourceBuilder, NOT_HYBRID, CHUNKED);
		SearcherService.applyTieBreak(searchSourceBuilder, NOT_HYBRID, CHUNKED);

		assertEquals(3, searchSourceBuilder.sorts().size());
	}

	@Test
	void should_resume_after_the_cursor_when_the_requested_sort_was_discarded() {
		// the requested sort was not sortable, so only the tie-break is left:
		// the cursor still has to be applied, or the next page repeats the first
		var searchSourceBuilder = new SearchSourceBuilder();
		SearcherService.applyTieBreak(searchSourceBuilder, NOT_HYBRID, NOT_CHUNKED);

		SearcherService.applySearchAfter(
			searchSourceBuilder, USER_SORT, cursor("[1.5,\"abc\"]"));

		assertArrayEquals(new Object[] {1.5, "abc"}, searchSourceBuilder.searchAfter());
	}

	@Test
	void should_ignore_the_cursor_when_no_sort_was_requested() {
		// a cursor taken with a user sort has more values than the clause of a
		// request without one, and OpenSearch answers 400 on the mismatch
		var searchSourceBuilder = new SearchSourceBuilder();
		SearcherService.applyTieBreak(searchSourceBuilder, NOT_HYBRID, NOT_CHUNKED);

		SearcherService.applySearchAfter(
			searchSourceBuilder, List.of(), cursor("[\"title\",\"title\",\"abc\"]"));
		SearcherService.applySearchAfter(
			searchSourceBuilder, null, cursor("[\"title\",\"title\",\"abc\"]"));

		assertNull(searchSourceBuilder.searchAfter());
	}

	@Test
	void should_not_resume_without_a_cursor() {
		var searchSourceBuilder = new SearchSourceBuilder();
		SearcherService.applyTieBreak(searchSourceBuilder, NOT_HYBRID, CHUNKED);

		SearcherService.applySearchAfter(searchSourceBuilder, USER_SORT, null);
		SearcherService.applySearchAfter(searchSourceBuilder, USER_SORT, "");

		assertNull(searchSourceBuilder.searchAfter());
	}

	@Test
	void should_not_resume_when_there_is_no_sort_clause() {
		// a hybrid query without a clause rejects search_after
		var searchSourceBuilder = new SearchSourceBuilder();
		SearcherService.applyTieBreak(searchSourceBuilder, HYBRID, CHUNKED);

		SearcherService.applySearchAfter(
			searchSourceBuilder, USER_SORT, cursor("[\"abc\"]"));

		assertNull(searchSourceBuilder.searchAfter());
	}

	private String cursor(String json) {
		return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * The sort criterion on the given field, or an assertion error when the
	 * request carries none.
	 */
	private FieldSortBuilder findSortOnField(
		SearchSourceBuilder searchSourceBuilder, String fieldName) {

		return searchSourceBuilder.sorts()
			.stream()
			.filter(FieldSortBuilder.class::isInstance)
			.map(FieldSortBuilder.class::cast)
			.filter(sort -> fieldName.equals(sort.getFieldName()))
			.findFirst()
			.orElseThrow(() -> new AssertionError(
				"no sort found on field " + fieldName));
	}

	/**
	 * The fields the given criteria sort by, in the order they are evaluated.
	 * Criteria that do not sort by a field, such as _score, are left out.
	 */
	private List<String> fieldSortNames(List<SortBuilder<?>> sorts) {
		return sorts
			.stream()
			.filter(FieldSortBuilder.class::isInstance)
			.map(FieldSortBuilder.class::cast)
			.map(FieldSortBuilder::getFieldName)
			.toList();
	}

}
