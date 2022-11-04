package io.openk9.searcher.mapper;

import com.google.protobuf.Descriptors;
import com.google.protobuf.MessageOrBuilder;
import io.openk9.searcher.grpc.QueryAnalysisResponse;
import io.openk9.searcher.payload.response.SuggestionsResponse;
import io.openk9.searcher.payload.response.suggestions.Suggestions;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
			case UNRECOGNIZED -> null;
		};
	}

}
