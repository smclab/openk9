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

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import io.openk9.filemanager.grpc.FileManagerGrpc;
import io.openk9.filemanager.grpc.FileResourceResponse;
import io.openk9.filemanager.grpc.FindFileResourceByResourceIdRequest;
import io.quarkus.grpc.GrpcClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import io.grpc.StatusRuntimeException;

@ApplicationScoped
public class DownloadService {

	/**
	 * Downloads a file from MinIO based on the provided resource ID and schema name.
	 * It first checks the database for the file's metadata and then retrieves the file from MinIO if available.
	 *
	 * @param resourceId The ID of the resource to download
	 * @param schemaName The schema name used to identify the bucket
	 * @return InputStream of the file if found, or a custom error response if not found or if an error occurs
	 */
	public InputStream downloadObject(String resourceId, String schemaName) {
		try {
			// Request to find the file resource by resourceId
			FindFileResourceByResourceIdRequest request = FindFileResourceByResourceIdRequest.newBuilder()
					.setResourceId(resourceId)
					.setSchemaName(schemaName)
					.build();

			// Query the file manager service to find the file's metadata
			FileResourceResponse response = filemanager.findFileResourceByResourceId(request);

			// Check if the file exists in the database
			if (response != null && !response.getFileId().isEmpty()) {
				// File metadata exists, proceed to download from MinIO
				String datasourceId = response.getDatasourceId();
				String fileId = response.getFileId();
				String bucketName = schemaName + "-datasource" + datasourceId;

				// Fetch the file from MinIO using the fileId and bucketName
				return minioClient.getObject(GetObjectArgs.builder()
						.bucket(bucketName)
						.object(fileId)
						.build());
			} else {
				// File not found in the database, log the missing file and return an empty InputStream
				logger.warn("File with resourceId " + resourceId + " not found in the database.");
				return InputStream.nullInputStream(); // Could also throw a custom exception if necessary
			}

		} catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
			// If the MinIO download fails, log the error and return null
			logger.error("MinIO download failed: " + e.getMessage(), e);
			return InputStream.nullInputStream(); // Return an empty InputStream on gRPC communication errors

		} catch (StatusRuntimeException e) {
			// Handle errors related to gRPC communication, such as unavailability or timeouts
			logger.error("gRPC error while communicating with filemanager service: " + e.getStatus().getDescription(), e);
			return InputStream.nullInputStream(); // Return an empty InputStream on gRPC communication errors

		} catch (Exception e) {
			// Catch any other unexpected exceptions and log them
			logger.error("Unexpected error occurred while downloading file: " + e.getMessage(), e);
			return InputStream.nullInputStream(); // Return an empty InputStream for any unexpected errors
		}
	}

	@GrpcClient("filemanager")
	FileManagerGrpc.FileManagerBlockingStub filemanager; // gRPC client to interact with the file manager service

	@Inject
	MinioClient minioClient; // MinIO client to interact with the MinIO server

	@Inject
	Logger logger; // Logger for logging information
}