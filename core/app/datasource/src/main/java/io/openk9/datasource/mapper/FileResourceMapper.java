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

package io.openk9.datasource.mapper;

import io.openk9.datasource.model.FileResource;
import io.openk9.datasource.model.dto.FileResourceDTO;
import io.openk9.filemanager.grpc.FileResourceRequest;
import io.openk9.filemanager.grpc.FileResourceResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(
	componentModel = "cdi"
)
public interface FileResourceMapper {

	FileResourceDTO toFileResourceDTO(FileResourceRequest fileResourceRequest);


	FileResourceResponse toFileResourceResponse(FileResource fileResource);
}