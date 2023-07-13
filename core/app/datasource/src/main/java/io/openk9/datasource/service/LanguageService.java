package io.openk9.datasource.service;

import io.openk9.datasource.mapper.LanguageMapper;
import io.openk9.datasource.model.Language;
import io.openk9.datasource.model.Language_;
import io.openk9.datasource.model.dto.LanguageDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LanguageService extends BaseK9EntityService<Language, LanguageDTO> {
	LanguageService(LanguageMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public Class<Language> getEntityClass() {
		return Language.class;
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {Language_.NAME};
	}


}
