package io.openk9.searcher.mapper;

import io.openk9.searcher.dto.ParserSearchToken;
import io.openk9.searcher.grpc.SearchTokenRequest;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
	componentModel = "cdi",
	collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED
)
public interface SearcherMapper {
	ParserSearchToken toParserSearchToken(SearchTokenRequest searchToken);

	SearchTokenRequest toQueryParserRequest(ParserSearchToken searchToken);

	List<SearchTokenRequest> toQueryParserRequest(List<ParserSearchToken> searchToken);

}
