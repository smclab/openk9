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
import io.openk9.filemanager.dto.ResourceDto;
import io.openk9.filemanager.model.Resource;
import io.openk9.filemanager.model.UploadRequestDto;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.core.eventbus.EventBus;
import org.jboss.logging.Logger;

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

	@Inject
	ResourceService resourceService;


	public InputStream downloadObject(String resourceId) {

		Resource resource = resourceService.findByResourceId(resourceId);
		String datasourceId = resource.getDatasourceId();
		String fileId = resource.getFileId();
		String state = String.valueOf(resource.getState());

		String bucketName = "datasource" + datasourceId;

		if (state.equalsIgnoreCase("Ok")) {
			try {
				return minioClient.getObject(
						GetObjectArgs.builder()
								.bucket(bucketName)
								.object(fileId)
								.build());
			} catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
				e.printStackTrace();
				return InputStream.nullInputStream();
			}
		}
		return null;
	}


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

	@ConsumeEvent(value = "upload", blocking = true)
	public void saveObject(UploadRequestDto uploadRequestDto) {

			String datasourceId = uploadRequestDto.getDatasourceId();
			String resourceId = uploadRequestDto.getResourceId();
			String fileId = uploadRequestDto.getFileId();
			InputStream inputStream = uploadRequestDto.getInputStream();

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
						.stream(inputStream,-1 , 1024L * 1024 * 5)
						.build();

				minioClient.putObject(args);

				logger.info("Upload done");

				ResourceDto resourceDto = new ResourceDto();
				resourceDto.setFileId(fileId);
				resourceDto.setDatasourceId(datasourceId);
				resourceDto.setVersion("1");
				resourceDto.setState(Resource.State.valueOf("OK"));
				resourceDto.setResourceId(resourceId);

				resourceService.update(uploadRequestDto.getId(), resourceDto);

			} catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
				System.out.println("Error occurred: " + e);

				ResourceDto resourceDto = new ResourceDto();
				resourceDto.setFileId(fileId);
				resourceDto.setDatasourceId(datasourceId);
				resourceDto.setVersion("1");
				resourceDto.setState(Resource.State.valueOf("KO"));
				resourceDto.setResourceId(resourceId);

				resourceService.update(uploadRequestDto.getId(), resourceDto);

			}

	}

	@Inject
	Logger logger;

	@Inject
	EventBus bus;


}