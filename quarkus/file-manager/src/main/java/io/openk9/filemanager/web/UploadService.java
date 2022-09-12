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

package io.openk9.filemanager.web;

import io.minio.*;
import io.minio.errors.*;
import io.openk9.filemanager.dto.ResourceDto;
import io.openk9.filemanager.model.Resource;
import io.openk9.filemanager.service.ResourceService;
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

	@Inject
	MinioClient minioClient;

	@Inject
	ResourceService resourceService;


	public Uni<String> uploadObject(InputStream inputStream, String datasourceId, String fileId, String resourceId) {


		ResourceDto resourceDto = new ResourceDto();
		resourceDto.setFileId(fileId);
		resourceDto.setDatasourceId(datasourceId);
		resourceDto.setVersion("1");
		resourceDto.setState(Resource.State.valueOf("PENDING"));
		resourceDto.setResourceId(resourceId);

		return resourceService.create(resourceDto).map(r -> {

			try {
				this.saveObject(datasourceId, fileId, inputStream);
			} catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
				throw new RuntimeException(e);
			}

			return resourceId;
		});

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

	private void saveObject(String datasourceId, String fileId, InputStream inputStream) throws ServerException,
			InsufficientDataException, ErrorResponseException,
			IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException,
			XmlParserException, InternalException {

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
		}
		else {
			logger.info("bucket already exist");
		}

		int length = inputStream.available();

		PutObjectArgs args = PutObjectArgs.builder()
				.bucket(datasourceId)
				.object(fileId)
				.stream(inputStream, length, -1)
				.build();

		minioClient.putObject(args);

	}

	@Inject
	Logger logger;


}