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

import io.openk9.datasource.mapper.DocTypeFieldMapper;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.dto.DocTypeFieldDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DocTypeFieldService extends BaseK9EntityService<DocTypeField, DocTypeFieldDTO> {
	 DocTypeFieldService(DocTypeFieldMapper mapper) {
		 this.mapper = mapper;
	}

	@Override
	public Class<DocTypeField> getEntityClass() {
		return DocTypeField.class;
	}
}
