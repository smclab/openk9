package io.openk9.datasource.service;

import io.openk9.datasource.mapper.TokenizerMapper;
import io.openk9.datasource.model.Tokenizer;
import io.openk9.datasource.model.Tokenizer_;
import io.openk9.datasource.model.dto.TokenizerDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TokenizerService extends BaseK9EntityService<Tokenizer, TokenizerDTO> {
	TokenizerService(TokenizerMapper mapper){this.mapper=mapper;}

	@Override
	public Class<Tokenizer> getEntityClass() {
		return Tokenizer.class;
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {Tokenizer_.NAME, Tokenizer_.DESCRIPTION};
	}
}
