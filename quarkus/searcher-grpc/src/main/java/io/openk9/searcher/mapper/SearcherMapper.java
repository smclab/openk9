package io.openk9.searcher.mapper;

import io.openk9.searcher.dto.ParserSearchToken;
import io.openk9.searcher.dto.SearchRequest;
import io.openk9.searcher.grpc.QueryParserRequest;
import io.openk9.searcher.grpc.SearchTokenRequest;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

import java.util.List;

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

}
