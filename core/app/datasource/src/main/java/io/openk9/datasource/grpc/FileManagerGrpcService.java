package io.openk9.datasource.grpc;

import io.openk9.datasource.mapper.FileResourceMapper;
import io.openk9.datasource.model.dto.FileResourceDTO;
import io.openk9.datasource.service.FileResourceService;
import io.openk9.filemanager.grpc.FileManager;
import io.openk9.filemanager.grpc.FileResourceRequest;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;

@GrpcService
public class FileManagerGrpcService implements FileManager {

	@Inject
	FileResourceService fileResourceService;

	@Inject
	FileResourceMapper fileResourceMapper;

	@Override
	public Uni<FileResourceRequest> findFileResource(
		FileResourceRequest request) {

		return fileResourceService.findByDatasourceAndFile(
			request.getDatasourceId(), request.getFileId())
			.onItem()
			.ifNotNull()
			.transform(fileResourceMapper::toFileResourceRequest);
	}

	@Override
	@ActivateRequestContext
	public Uni<FileResourceRequest> findFileResourceByResourceId(
		FileResourceRequest request) {

		return fileResourceService.findByResourceId(
			request.getResourceId()
			)
			.onItem()
			.ifNotNull()
			.transform(fileResourceMapper::toFileResourceRequest);
	}

	@Override
	public Uni<FileResourceRequest> createFileResource(
		FileResourceRequest request) {

		FileResourceDTO fileResourceDTO = fileResourceMapper.toFileResourceDTO(request);

		return fileResourceService.create(fileResourceDTO)
			.onItem()
			.ifNotNull()
			.transform(fileResourceMapper::toFileResourceRequest);
	}
}
