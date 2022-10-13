package io.openk9.datasource.mapper;

import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.model.dto.TokenTabDTO;
import io.openk9.datasource.web.SearchTokenDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
	config = K9EntityMapper.class
)
public interface TokenTabMapper extends K9EntityMapper<TokenTab, TokenTabDTO>{

	@Mapping(target = "values",source = "value")
	public SearchTokenDto toSearchTokenDto(TokenTab tokenTab);

	default List<String> map(String value){
		return List.of(value);
	}


}
