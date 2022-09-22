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

package io.openk9.ingestion.client.filemanager;

import io.openk9.ingestion.client.filemanager.exception.FileManagerException;
import io.quarkus.rest.client.reactive.ClientExceptionMapper;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@Path("/v1/file-manager")
@RegisterRestClient(configKey = "file-manager")
public interface FileManagerClient {

	@GET
	@Path("/download/{resourceId}")
	InputStream download(@PathParam("resourceId") String resourceId);

	@POST
	@Path("/delete/{resourceId}")
	void delete(@PathParam("resourceId") String resourceId);

	@POST
	@Path("/upload/{datasourceId}/{fileId}")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	String upload(@PathParam("datasourceId") String datasourceId, @PathParam("fileId") String fileId,
				InputStream inputStream);


	@ClientExceptionMapper
	static FileManagerException toException(Response response) {

		if (response.getStatus() == 500) {
			return new FileManagerException("error during download file");
		}

		return null;
	}
}
