package io.openk9.datasource.grpc;

import io.openk9.datasource.mapper.FileResourceMapper;
import io.openk9.datasource.model.dto.FileResourceDTO;
import io.openk9.datasource.service.FileResourceService;
import io.openk9.filemanager.grpc.FileManager;
import io.openk9.filemanager.grpc.FileResourceRequest;
import io.openk9.filemanager.grpc.FileResourceResponse;
import io.openk9.filemanager.grpc.FindFileResourceByDatasourceIdFileIdRequest;
import io.openk9.filemanager.grpc.FindFileResourceByResourceIdRequest;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;

@GrpcService
public class FileManagerGrpcService implements FileManager {

	@Inject
	FileResourceService fileResourceService;

	@Inject
	FileResourceMapper fileResourceMapper;

	@Override
	public Uni<FileResourceResponse> findFileResourceByResourceId(
		FindFileResourceByResourceIdRequest request) {

		return fileResourceService.findByResourceId(
				request.getResourceId()
			)
			.onItem()
			.ifNotNull()
			.transform(fileResourceMapper::toFileResourceResponse);

	}

	@Override
	public Uni<FileResourceResponse> findFileResourceByDatasourceIdAndFileId(
		FindFileResourceByDatasourceIdFileIdRequest request) {

		return fileResourceService.findByDatasourceAndFile(
				request.getDatasourceId(), request.getFileId()
			)
			.onItem()
			.ifNotNull()
			.transform(fileResourceMapper::toFileResourceResponse);
	}

	@Override
	public Uni<FileResourceResponse> createFileResource(
		FileResourceRequest request) {

		FileResourceDTO fileResourceDTO = fileResourceMapper.toFileResourceDTO(request);

		return fileResourceService.create(fileResourceDTO)
			.onItem()
			.ifNotNull()
			.transform(fileResourceMapper::toFileResourceResponse);
	}
}
