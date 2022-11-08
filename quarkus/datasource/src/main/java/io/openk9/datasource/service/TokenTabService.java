package io.openk9.datasource.service;

import io.openk9.datasource.mapper.DocTypeFieldMapper;
import io.openk9.datasource.mapper.TokenTabMapper;
import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.model.TokenTab_;
import io.openk9.datasource.model.dto.TokenTabDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;

import javax.enterprise.context.RequestScoped;

@RequestScoped
public class TokenTabService extends BaseK9EntityService<TokenTab, TokenTabDTO> {
	TokenTabService(TokenTabMapper mapper) {this.mapper = mapper;}

	@Override
	public Class<TokenTab> getEntityClass(){
		return TokenTab.class;
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {TokenTab_.NAME, TokenTab_.TOKEN_TYPE,TokenTab_.KEYWORD_KEY};
	}

}
