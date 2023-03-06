package io.openk9.searcher.client.mapper;

import io.openk9.searcher.client.dto.ParserSearchToken;
import io.openk9.searcher.client.dto.SearchRequest;
import io.openk9.searcher.grpc.QueryParserRequest;
import io.openk9.searcher.grpc.SearchTokenRequest;
import io.openk9.searcher.grpc.Sort;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

import java.util.List;
import java.util.Map;

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
