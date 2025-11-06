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

import io.openk9.datasource.enricher.HttpEnricherClient;
import io.openk9.datasource.web.dto.ResourceUriDTO;
import io.openk9.datasource.model.form.FormTemplate;
import io.openk9.datasource.web.dto.HealthDTO;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import io.openk9.datasource.mapper.EnrichItemMapper;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.EnrichItem_;
import io.openk9.datasource.model.dto.base.EnrichItemDTO;
import jakarta.inject.Inject;

@ApplicationScoped
public class EnrichItemService extends BaseK9EntityService<EnrichItem, EnrichItemDTO> {

    @Inject
    HttpEnricherClient httpEnricherClient;

	EnrichItemService(EnrichItemMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public Class<EnrichItem> getEntityClass() {
		return EnrichItem.class;
	}

	@Override
	public String[] getSearchFields() {
		return new String[]{EnrichItem_.NAME, EnrichItem_.TYPE, EnrichItem_.SERVICE_NAME};
	}

    public Uni<FormTemplate> getForm(String serviceName) {
        ResourceUriDTO resourceUriDTO = ResourceUriDTO.builder()
			.baseUri(serviceName)
			.build();
        return httpEnricherClient.getForm(resourceUriDTO);
    }

    public Uni<HealthDTO> getHealth(String serviceName) {
        ResourceUriDTO resourceUriDTO = ResourceUriDTO.builder()
			.baseUri(serviceName)
			.build();
        return httpEnricherClient.getHealth(resourceUriDTO);
    }

}
