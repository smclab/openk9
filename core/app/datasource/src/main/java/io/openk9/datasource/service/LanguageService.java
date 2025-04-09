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

import jakarta.enterprise.context.ApplicationScoped;

import io.openk9.datasource.mapper.LanguageMapper;
import io.openk9.datasource.model.Language;
import io.openk9.datasource.model.Language_;
import io.openk9.datasource.model.dto.base.LanguageDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;

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
