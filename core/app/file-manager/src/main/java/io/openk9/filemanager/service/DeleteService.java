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

package io.openk9.filemanager.service;

import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.openk9.filemanager.grpc.FileManagerGrpc;
import io.openk9.filemanager.grpc.FileResourceResponse;
import io.openk9.filemanager.grpc.FindFileResourceByResourceIdRequest;
import io.quarkus.grpc.GrpcClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class DeleteService {

	// Method to delete an object from MinIO and file resource from the database
	public void deleteObject(String resourceId, String schemaName) {

		try {

			// Creating request to find file resource by resourceId and schemaName
			FindFileResourceByResourceIdRequest findFileResourceByResourceIdRequest =
					FindFileResourceByResourceIdRequest.newBuilder()
							.setResourceId(resourceId)  // Set the resource ID for lookup
							.setSchemaName(schemaName)   // Set the schema name for the resource
							.build();  // Build the request object

			// Calling the GRPC service to find the file resource by resourceId
			FileResourceResponse fileResourceResponse =
					filemanager.findFileResourceByResourceId(findFileResourceByResourceIdRequest);

			// Extracting the datasourceId and fileId from the response
			String datasourceId = fileResourceResponse.getDatasourceId();
			String fileId = fileResourceResponse.getFileId();

			// Constructing the bucket name using schema name and datasource ID
			String bucketName = schemaName + "-datasource" + datasourceId;

			// Logging the removal of the object from MinIO
			logger.info("Removing object with fileId " + fileId + " in bucket " + bucketName);

			// Removing the object from the MinIO bucket using fileId
			minioClient.removeObject(
					RemoveObjectArgs.builder()
							.bucket(bucketName)  // Specify the bucket
							.object(fileId)      // Specify the object (file) ID to remove
							.build());           // Build the remove object request

			// Logging that the object was removed from the storage
			logger.info("Removed object with resourceId: " + resourceId);

			// Deleting the file resource from the database using filemanager service
			filemanager.deleteFileResource(findFileResourceByResourceIdRequest);

			// Logging the successful removal of the file resource
			logger.info("Removed entity with resourceId: " + resourceId);

		} catch (Exception e) {
			// Logging any errors that occur during the delete operation
			logger.info("Delete failed with exception: " + e.getMessage());
		}
	}

	@GrpcClient("filemanager")  // GRPC client injection for FileManager service
	FileManagerGrpc.FileManagerBlockingStub filemanager;  // Blocking stub for FileManager GRPC service

	@Inject
	Logger logger;  // Injecting Logger to log messages

	@Inject
	MinioClient minioClient;  // Inject MinioClient to interact with MinIO
}
