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

import io.minio.errors.*;
import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Path("/v1/file-manager/upload")
public class UploadEndpoint {

	@POST
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Path("/{datasourceId}/{fileId}")
	public String upload(@PathParam("datasourceId") String datasourceId, @PathParam("fileId") String fileId,
					  InputStream inputStream) throws IOException, ServerException, InsufficientDataException,
			ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException,
			XmlParserException, InternalException {

		String resourceId = UUID.randomUUID().toString();

		uploadService.uploadObject(inputStream, datasourceId, fileId, resourceId);

		return resourceId;

	}

	@Inject
	UploadService uploadService;

}