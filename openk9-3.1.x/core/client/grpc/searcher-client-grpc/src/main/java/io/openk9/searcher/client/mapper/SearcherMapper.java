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

import java.util.List;
import java.util.Map;

import io.openk9.searcher.client.dto.ParserSearchToken;
import io.openk9.searcher.client.dto.SearchRequest;
import io.openk9.searcher.grpc.QueryParserRequest;
import io.openk9.searcher.grpc.SearchTokenRequest;
import io.openk9.searcher.grpc.Sort;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(
	componentModel = "cdi",
	collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
	nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface SearcherMapper {

	QueryParserRequest toQueryParserRequest(SearchRequest searchRequest);

	ParserSearchToken toParserSearchToken(SearchTokenRequest searchToken);

	SearchTokenRequest toQueryParserRequest(ParserSearchToken searchToken);

	List<SearchTokenRequest> toQueryParserRequest(List<ParserSearchToken> searchToken);

	static Sort toSort(Map<String, Map<String,String>> value) {

		for (Map.Entry<String, Map<String, String>> entry : value.entrySet()) {
			Sort.Builder builder = Sort.newBuilder();
			builder.setField(entry.getKey());
			Map<String, String> sortValue = entry.getValue();
			if (sortValue != null) {
				for (Map.Entry<String, String> entry1 : sortValue.entrySet()) {
					builder.putExtras(entry1.getKey(), entry1.getValue());
				}
			}
			return builder.build();
		}

		return Sort.getDefaultInstance();
	}

}
