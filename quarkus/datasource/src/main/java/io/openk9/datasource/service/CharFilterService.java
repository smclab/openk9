package io.openk9.datasource.service;

import io.openk9.datasource.mapper.CharFilterMapper;

import io.openk9.datasource.model.CharFilter;
import io.openk9.datasource.model.CharFilter_;
import io.openk9.datasource.model.dto.CharFilterDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CharFilterService extends BaseK9EntityService<CharFilter, CharFilterDTO> {
	CharFilterService(CharFilterMapper mapper){this.mapper=mapper;}

	@Override
	public Class<CharFilter> getEntityClass() {
		return CharFilter.class;
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {CharFilter_.NAME, CharFilter_.DESCRIPTION};
	}

}
