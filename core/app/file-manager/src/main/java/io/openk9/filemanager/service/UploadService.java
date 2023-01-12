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

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import io.openk9.filemanager.grpc.FileManagerGrpc;
import io.openk9.filemanager.grpc.FileManager;
import io.openk9.filemanager.grpc.FileResourceRequest;
import io.quarkus.grpc.GrpcClient;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@ApplicationScoped
public class UploadService {

	@ConfigProperty(name = "part.size")
	long partSize;

	@Inject
	MinioClient minioClient;


	public String uploadObject(InputStream inputStream, String datasourceId, String fileId) {

		try {
			String bucketName = "datasource" + datasourceId;

			boolean found =
					minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

			if (!found) {
				// Create bucket with default region.
				logger.info("bucket not exist.");
				minioClient.makeBucket(
						MakeBucketArgs.builder()
								.bucket(bucketName)
								.build());
			} else {
				logger.info("bucket already exist");
			}

			PutObjectArgs args = PutObjectArgs.builder()
					.bucket(bucketName)
					.object(fileId)
					.stream(inputStream,-1 , partSize)
					.build();

			minioClient.putObject(args);

			logger.info("Upload done");

			FileResourceRequest fileResourceRequest = FileResourceRequest.newBuilder()
				.setDatasourceId(datasourceId)
				.setFileId(fileId).setResourceId("test").build();

			logger.info(fileResourceRequest.toString());

			FileResourceRequest fileResourceRequest_ =
				filemanager.findFileResourceByResourceId(fileResourceRequest);

			String resourceId;

			if (fileResourceRequest_ == null) {

				logger.info("Resource not exist. Creating in database.");

				resourceId = UUID.randomUUID().toString();

				FileResourceRequest fileResourceRequest1 = FileResourceRequest.newBuilder()
					.setDatasourceId(datasourceId)
					.setFileId(fileId).setResourceId(resourceId).build();

				filemanager.createFileResource(fileResourceRequest1);

			}
			else {

				logger.info("Resource already exist. Skip persist.");

				resourceId = fileResourceRequest.getResourceId();
			}

			return null;


		} catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
			return null;
		}

	}

	@Inject
	Logger logger;

	@GrpcClient("filemanager")
	FileManagerGrpc.FileManagerBlockingStub filemanager;


}