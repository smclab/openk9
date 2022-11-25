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

import io.openk9.datasource.mapper.PluginDriverMapper;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.PluginDriver_;
import io.openk9.datasource.model.dto.PluginDriverDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PluginDriverService extends BaseK9EntityService<PluginDriver, PluginDriverDTO> {
	 PluginDriverService(PluginDriverMapper mapper) {
		 this.mapper = mapper;
	}

	@Override
	public Class<PluginDriver> getEntityClass() {
		return PluginDriver.class;
	}

	@Override
	public String[] getSearchFields() {
		return new String[]{PluginDriver_.NAME, PluginDriver_.DESCRIPTION, PluginDriver_.TYPE};
	}
}