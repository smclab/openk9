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

package com.openk9.search.client.internal.search;

import com.openk9.search.client.api.ReactorActionListener;
import com.openk9.search.client.api.RestHighLevelClientProvider;
import com.openk9.search.client.api.Search;
import com.openk9.search.client.api.SearchRequestFactory;
import com.openk9.search.client.api.util.SearchUtil;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component(
	immediate = true,
	service = Search.class
)
public class SearchImpl implements Search {

	@Override
	public Mono<SearchResponse> search(SearchRequest searchRequest) {

		if (searchRequest == SearchUtil.EMPTY_SEARCH_REQUEST) {
			return Mono.just(SearchUtil.EMPTY_SEARCH_RESPONSE);
		}

		return Mono.create(
			sink -> _restHighLevelClientProvider.get().searchAsync(
				searchRequest, RequestOptions.DEFAULT,
				new ReactorActionListener<>(sink)));
	}

	@Override
	public Mono<SearchResponse> search(
		Function<SearchRequestFactory, SearchRequest> function) {

		return Mono.defer(() -> search(function.apply(_searchRequestFactory)));
	}

	@Reference
	private RestHighLevelClientProvider _restHighLevelClientProvider;

	@Reference
	private SearchRequestFactory _searchRequestFactory;

	private static final Object _EMPTY_OBJECT = new Object();

}
