package io.openk9.datasource.service;

import io.openk9.datasource.mapper.TokenFilterMapper;
import io.openk9.datasource.model.TokenFilter;
import io.openk9.datasource.model.TokenFilter_;
import io.openk9.datasource.model.dto.TokenFilterDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class TokenFilterService extends BaseK9EntityService<TokenFilter, TokenFilterDTO> {
	TokenFilterService(TokenFilterMapper mapper) {this.mapper = mapper;}

	@Override
	public Class<TokenFilter> getEntityClass() {
		return TokenFilter.class;
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {TokenFilter_.NAME, TokenFilter_.DESCRIPTION};
	}
}
