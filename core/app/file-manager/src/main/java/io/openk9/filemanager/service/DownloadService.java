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

import io.minio.*;
import io.minio.errors.MinioException;
import io.openk9.filemanager.grpc.FileManagerGrpc;
import io.openk9.filemanager.grpc.FileResourceResponse;
import io.openk9.filemanager.grpc.FindFileResourceByResourceIdRequest;
import io.quarkus.grpc.GrpcClient;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@ApplicationScoped
public class DownloadService {

	@Inject
	MinioClient minioClient;


	public InputStream downloadObject(String resourceId, String schemaName) {

		try {

			FindFileResourceByResourceIdRequest findFileResourceByResourceIdRequest =
				FindFileResourceByResourceIdRequest.newBuilder()
					.setResourceId(resourceId)
					.setSchemaName(schemaName).build();

			FileResourceResponse fileResourceResponse =
				filemanager.findFileResourceByResourceId(findFileResourceByResourceIdRequest);

			if (fileResourceResponse != null) {

				String datasourceId = fileResourceResponse.getDatasourceId();
				String fileId = fileResourceResponse.getFileId();

				String bucketName = "datasource" + datasourceId;

				return minioClient.getObject(
						GetObjectArgs.builder()
								.bucket(bucketName)
								.object(fileId)
								.build());
			}
			else {
				return InputStream.nullInputStream();
			}
		} catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
				return InputStream.nullInputStream();
		}
	}

	@GrpcClient("filemanager")
	FileManagerGrpc.FileManagerBlockingStub filemanager;


	/*public boolean isObjectExist(String bucketName, String objectName) {
		try {
			minioClient.statObject(StatObjectArgs.builder()
					.bucket(bucketName)
					.object(objectName).build());
			return true;
		} catch (ErrorResponseException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}*/

}