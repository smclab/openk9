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

import io.openk9.filemanager.service.DownloadService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.reactive.RestResponse;

import java.io.IOException;
import java.io.InputStream;

@Path("/v1/download/byte")
public class DownloadAsByteArray {

	@GET
	@Path("/{resourceId}/{schemaName}")
	public RestResponse<byte[]> downloadAsByte(@PathParam("resourceId") String resourceId,
											   @PathParam("schemaName") String schemaName) {

		InputStream inputStream = downloadService.
			downloadObject(resourceId, schemaName);

		byte[] sourceBytes;

		try {
			sourceBytes = IOUtils.toByteArray(inputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return RestResponse.ResponseBuilder.ok(sourceBytes)
			// set a response header
			.header("Content-Type", "application/pdf")
			// end of builder API
			.build();

	}

	@Inject
	DownloadService downloadService;

}