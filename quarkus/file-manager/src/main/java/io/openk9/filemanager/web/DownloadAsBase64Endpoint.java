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

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import org.apache.commons.io.IOUtils;

@Path("/v1/file-manager/download")
public class DownloadAsBase64Endpoint {

	@GET
	@Path("/{resourceId}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public String downloadAsBase64(@PathParam("resourceId") String resourceId) {

		InputStream inputStream = downloadService.downloadObject(resourceId);

		byte[] sourceBytes;

		try {
			sourceBytes = IOUtils.toByteArray(inputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return Base64.getEncoder().encodeToString(sourceBytes);

	}

	@Inject
	DownloadService downloadService;

}