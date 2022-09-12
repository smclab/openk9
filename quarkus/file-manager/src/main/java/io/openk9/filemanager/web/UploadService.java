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
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@ApplicationScoped
public class UploadService {

	@Inject
	MinioClient minioClient;


	public String uploadObject(InputStream inputStream, String datasourceId, String fileId, String dataId) throws
			IOException, ServerException, InsufficientDataException, ErrorResponseException,
			NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException,
			InternalException {

		int length = inputStream.available();

		String bucketName = "datasource" + datasourceId;

		boolean found =
				minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
		if (!found) {
			// Create bucket with default region.
			minioClient.makeBucket(
				MakeBucketArgs.builder()
						.bucket(bucketName)
						.build());
		}

		PutObjectArgs args = PutObjectArgs.builder()
				.bucket(datasourceId)
				.object(fileId)
				.stream(inputStream, length, -1)
				.build();

		try {
			ObjectWriteResponse response = minioClient.putObject(args);
			return response.toString();
		} catch (MinioException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		}
	}


}