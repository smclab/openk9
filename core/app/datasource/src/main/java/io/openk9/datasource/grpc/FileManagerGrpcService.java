package io.openk9.datasource.grpc;

import io.openk9.auth.tenant.TenantResolver;
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

	@Inject
	TenantResolver _tenantResolver;

	@Override
	public Uni<FileResourceResponse> findFileResourceByResourceId(
		FindFileResourceByResourceIdRequest request) {

		_tenantResolver.setTenant(request.getSchemaName());

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

		_tenantResolver.setTenant(request.getSchemaName());

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

		_tenantResolver.setTenant(request.getSchemaName());

		FileResourceDTO fileResourceDTO = fileResourceMapper.toFileResourceDTO(request);

		return fileResourceService.create(fileResourceDTO)
			.onItem()
			.ifNotNull()
			.transform(fileResourceMapper::toFileResourceResponse);
	}

	@Override
	public Uni<com.google.protobuf.Empty> deleteFileResource(
		FindFileResourceByResourceIdRequest request) {

		_tenantResolver.setTenant(request.getSchemaName());

		return fileResourceService.deleteFileResource(request.getResourceId());
	}
}
