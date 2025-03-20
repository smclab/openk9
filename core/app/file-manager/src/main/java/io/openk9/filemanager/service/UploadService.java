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

import io.grpc.StatusRuntimeException;
import io.minio.*;
import io.minio.errors.MinioException;
import io.openk9.filemanager.grpc.*;
import io.quarkus.grpc.GrpcClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@ApplicationScoped
public class UploadService {

	@ConfigProperty(name = "part.size")
	long partSize; // The part size for uploading to MinIO

	/**
	 * Uploads a file to MinIO and then creates a database record for it.
	 * If the database write fails, the file is removed from MinIO.
	 *
	 * @param inputStream  Input stream of the file to upload
	 * @param datasourceId ID of the datasource
	 * @param fileId       ID of the file
	 * @param schemaName   Name of the schema
	 * @return resourceId if upload succeeds, null otherwise
	 */
	public String uploadObject(InputStream inputStream, String datasourceId, String fileId, String schemaName) {
		String bucketName = schemaName + "-datasource" + datasourceId;

		try {
			// Ensure the MinIO bucket exists
			ensureBucketExists(bucketName);

			// Upload the file to MinIO
			PutObjectArgs args = PutObjectArgs.builder()
					.bucket(bucketName)
					.object(fileId)
					.stream(inputStream, -1, partSize) // -1 indicates the size is unknown
					.build();

			minioClient.putObject(args); // Perform the upload to MinIO
			logger.info("Upload successful for fileId: " + fileId);

			// After the successful upload, create the database record
			String resourceId = ensureDatabaseRecordExistsOrCreate(datasourceId, fileId, schemaName);
			logger.info("Database record created for resourceId: " + resourceId);

			return resourceId; // Return the resourceId if everything succeeds

		} catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
			// If the MinIO upload fails, log the error and return null
			logger.error("MinIO upload failed: " + e.getMessage(), e);
			return null;

		} catch (StatusRuntimeException e) {
			// iI error while communicating with filemanager service, roll back the file from MinIO
			logger.error("gRPC error while communicating with filemanager service: " + e.getStatus().getDescription(), e);
			rollbackUpload(bucketName, fileId); // Rollback MinIO file upload
			return null;

		} catch (Exception e) {
			// Catch any other unexpected exceptions and log them
			logger.error("Unexpected error occurred while uploading file: " + e.getMessage(), e);
			return null;
	}
	}

	/**
	 * Checks if the MinIO bucket exists. If it doesn't, it creates it.
	 *
	 * @param bucketName Name of the bucket to check/create
	 */
	private void ensureBucketExists(String bucketName) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
		boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

		if (!found) {
			// If the bucket doesn't exist, create it
			logger.info("Bucket does not exist, creating: " + bucketName);
			minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
		} else {
			// If the bucket already exists, log that information
			logger.info("Bucket already exists: " + bucketName);
		}
	}

	private String ensureDatabaseRecordExistsOrCreate(String datasourceId, String fileId, String schemaName) {

		FindFileResourceByDatasourceIdFileIdRequest findRequest =
				FindFileResourceByDatasourceIdFileIdRequest.newBuilder()
						.setDatasourceId(datasourceId)
						.setFileId(fileId)
						.setSchemaName(schemaName)
						.build();

		FileResourceResponse fileResourceResponse =
				filemanager.findFileResourceByDatasourceIdAndFileId(findRequest);

		if (fileResourceResponse.getResourceId().isBlank()) {
			logger.info("Resource not found, creating in database.");

			String resourceId = UUID.randomUUID().toString();
			FileResourceRequest createRequest = FileResourceRequest.newBuilder()
					.setDatasourceId(datasourceId)
					.setFileId(fileId)
					.setResourceId(resourceId)
					.setSchemaName(schemaName)
					.build();

			filemanager.createFileResource(createRequest);
			return resourceId;
		} else {
			logger.info("Resource already exists, skipping persist.");
			return fileResourceResponse.getResourceId();
		}
	}

	/**
	 * Rolls back the uploaded file in MinIO if gRPC error while communicating with filemanager service.
	 * Removes the uploaded file from MinIO to avoid orphaned files.
	 *
	 * @param bucketName Name of the MinIO bucket
	 * @param fileId     ID of the file to remove
	 */
	private void rollbackUpload(String bucketName, String fileId) {
		try {
			// Remove the file from MinIO if gRPC error while communicating with filemanager service
			logger.warn("Rolling back upload, removing file: " + fileId);
			minioClient.removeObject(RemoveObjectArgs.builder()
					.bucket(bucketName)
					.object(fileId)
					.build());
		} catch (Exception e) {
			// Log any errors encountered during the rollback (i.e., file may still exist in MinIO)
			logger.error("Rollback failed, file may remain orphaned in MinIO: " + fileId, e);
		}
	}

	@GrpcClient("filemanager")
	FileManagerGrpc.FileManagerBlockingStub filemanager; // gRPC client to interact with the database


	@Inject
	MinioClient minioClient; // Client to interact with MinIO

	@Inject
	Logger logger; // Logger to log messages
}
