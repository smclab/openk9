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

package io.openk9.searcher.mapper;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.openk9.searcher.grpc.QueryAnalysisResponse;
import io.openk9.searcher.payload.response.SuggestionsResponse;
import io.openk9.searcher.payload.response.suggestions.Suggestions;

import com.google.protobuf.Descriptors;
import com.google.protobuf.MessageOrBuilder;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
	componentModel = "cdi",
	collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
	nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
	unmappedSourcePolicy = ReportingPolicy.WARN,
	unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface InternalSearcherMapper {

	SuggestionsResponse toSuggestionsResponse(
		io.openk9.searcher.grpc.SuggestionsResponse result);

	io.openk9.searcher.queryanalysis.QueryAnalysisResponse toQueryAnalysisResponse(
		QueryAnalysisResponse queryAnalysisResponse);

	default Map<String, Object> mapMessageToMap(MessageOrBuilder messageOrBuilder) {

		Map<String, Object> map = new LinkedHashMap<>();

		for (Map.Entry<Descriptors.FieldDescriptor, Object> entry : messageOrBuilder.getAllFields().entrySet()) {
			map.put(entry.getKey().getName(), entry.getValue());
		}

		return map;
	}

	default Collection<Object> map(List<io.openk9.searcher.grpc.Suggestions> value) {
		if ( value == null ) {
			return List.of();
		}

		Collection<Object> collection = new java.util.ArrayList<>( value.size() );
		for ( io.openk9.searcher.grpc.Suggestions suggestions : value ) {
			if ( suggestions == null ) {
				continue;
			}
			collection.add( map( suggestions ) );
		}

		return collection;
	}

	default Suggestions map(io.openk9.searcher.grpc.Suggestions value) {
		if (value == null) {
			return null;
		}

		return switch (value.getTokenType()) {
			case ENTITY -> Suggestions.entity(
				value.getValue(), value.getSuggestionCategoryId(),
				value.getEntityType(), value.getEntityValue(), value.getKeywordKey(),
				value.getCount());
			case TEXT -> Suggestions.text(
				value.getValue(), value.getSuggestionCategoryId(), value.getKeywordKey(),
				value.getCount());
			case DOCTYPE -> Suggestions.docType(
				value.getValue(), value.getSuggestionCategoryId(),
				value.getCount());
			case FILTER -> Suggestions.filter(
				value.getValue(), value.getSuggestionCategoryId(), value.getKeywordKey(),
				value.getCount());
			default -> null;
		};

	}

}
