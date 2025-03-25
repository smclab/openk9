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

package io.openk9.datasource.grpc;

import jakarta.inject.Inject;

import io.openk9.datasource.mapper.FileResourceMapper;
import io.openk9.datasource.model.dto.base.FileResourceDTO;
import io.openk9.datasource.service.FileResourceService;
import io.openk9.filemanager.grpc.FileManager;
import io.openk9.filemanager.grpc.FileResourceRequest;
import io.openk9.filemanager.grpc.FileResourceResponse;
import io.openk9.filemanager.grpc.FindFileResourceByDatasourceIdFileIdRequest;
import io.openk9.filemanager.grpc.FindFileResourceByResourceIdRequest;

import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;

@GrpcService
public class FileManagerGrpcService implements FileManager {

	@Inject
	FileResourceService fileResourceService;

	@Inject
	FileResourceMapper fileResourceMapper;

	@Override
	public Uni<FileResourceResponse> findFileResourceByResourceId(
		FindFileResourceByResourceIdRequest request) {

		return fileResourceService
			.findByResourceId(request.getSchemaName(), request.getResourceId())
			.onItem()
			.ifNotNull()
			.transform(fileResourceMapper::toFileResourceResponse);

	}

	@Override
	public Uni<FileResourceResponse> findFileResourceByDatasourceIdAndFileId(
		FindFileResourceByDatasourceIdFileIdRequest request) {

		return fileResourceService
			.findByDatasourceAndFile(
				request.getSchemaName(), request.getDatasourceId(), request.getFileId())
			.onItem()
			.ifNotNull()
			.transform(fileResourceMapper::toFileResourceResponse);

	}

	@Override
	public Uni<FileResourceResponse> createFileResource(
		FileResourceRequest request) {

		FileResourceDTO fileResourceDTO = fileResourceMapper.toFileResourceDTO(request);

		return fileResourceService
			.create(request.getSchemaName(), fileResourceDTO)
			.onItem()
			.ifNotNull()
			.transform(fileResourceMapper::toFileResourceResponse);

	}

	@Override
	public Uni<com.google.protobuf.Empty> deleteFileResource(
		FindFileResourceByResourceIdRequest request) {

		return fileResourceService
			.deleteFileResource(request.getSchemaName(), request.getResourceId());

	}
}
