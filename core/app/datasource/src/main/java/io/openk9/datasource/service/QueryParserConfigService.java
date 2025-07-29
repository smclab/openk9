/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.datasource.service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;

import io.openk9.datasource.mapper.QueryParserConfigMapper;
import io.openk9.datasource.model.QueryAnalysis_;
import io.openk9.datasource.model.QueryParserConfig;
import io.openk9.datasource.model.QueryParserType;
import io.openk9.datasource.model.dto.base.QueryParserConfigDTO;
import io.openk9.datasource.model.form.FormConfigurations;
import io.openk9.datasource.model.form.FormTemplate;
import io.openk9.datasource.model.init.QueryParserConfigs;

@ApplicationScoped
public class QueryParserConfigService extends BaseK9EntityService<QueryParserConfig, QueryParserConfigDTO> {
	 QueryParserConfigService(QueryParserConfigMapper mapper) {
		 this.mapper = mapper;
	}

	@Override
	public Class<QueryParserConfig> getEntityClass() {
		return QueryParserConfig.class;
	}

	/**
	 * Retrieves the cached {@link FormConfigurations} object derived from predefined form templates.
	 * <p>
	 * The configurations are generated once when this service is initialized, by processing
	 * {@link QueryParserConfigs#FORM_TEMPLATES}. Subsequent calls return the same cached instance.
	 *
	 * @return The cached {@link FormConfigurations} instance containing a list of
	 * all derived {@link FormConfigurations.FormConfiguration} objects.
	 * @see QueryParserConfigs#FORM_TEMPLATES
	 * @see FormConfigurations
	 * @see QueryParserType
	 * @see FormTemplate
	 */
	public FormConfigurations getFormConfigurations() {
		return CACHED_FORM_CONFIGURATIONS;
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {QueryAnalysis_.NAME, QueryAnalysis_.DESCRIPTION};
	}

	private static FormConfigurations initFormConfigurations() {

		// initialize the FormConfigurations cached instance

		var formTemplateEntries = QueryParserConfigs.FORM_TEMPLATES.entrySet();

		List<FormConfigurations.FormConfiguration> configurations = new LinkedList<>();

		// Mapping entry to form configuration
		for (Map.Entry<QueryParserType, FormTemplate> entry : formTemplateEntries) {

			var configuration = new FormConfigurations.FormConfiguration(
				entry.getKey().name(), // convert enum to string
				entry.getValue()
			);

			configurations.add(configuration);

		}

		return new FormConfigurations(configurations);
	}

	private static final FormConfigurations CACHED_FORM_CONFIGURATIONS = initFormConfigurations();
}
