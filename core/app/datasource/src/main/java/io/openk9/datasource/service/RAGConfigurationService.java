package io.openk9.datasource.service;

import io.openk9.datasource.mapper.RAGConfigurationMapper;
import io.openk9.datasource.model.RAGConfiguration;
import io.openk9.datasource.model.dto.RAGConfigurationDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RAGConfigurationService
	extends BaseK9EntityService<RAGConfiguration, RAGConfigurationDTO> {

	RAGConfigurationService(RAGConfigurationMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public Class<RAGConfiguration> getEntityClass() {
		return RAGConfiguration.class;
	}
}
