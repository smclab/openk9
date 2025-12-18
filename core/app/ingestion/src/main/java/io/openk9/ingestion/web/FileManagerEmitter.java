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

package io.openk9.ingestion.web;

import io.openk9.ingestion.client.filemanager.FileManagerClient;
import io.openk9.ingestion.dto.BinaryDTO;
import io.openk9.ingestion.dto.IngestionDTO;
import io.openk9.ingestion.dto.ResourcesDTO;
import io.openk9.ingestion.exception.NoSuchQueueException;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class FileManagerEmitter {

	@Inject
	IngestionEmitter emitter;
	@Inject
	@RestClient
	FileManagerClient fileManagerClient;
	@Inject
	Logger logger;

	public Uni<Void> emit(IngestionDTO ingestionDTO) {

		return Uni.createFrom()
			.deferred(() -> {

				if (ingestionDTO.getResources() != null
					&& ingestionDTO.getResources().getBinaries() != null
					&& !ingestionDTO.getResources().getBinaries().isEmpty()) {

					logger.info("Handling binaries");

					String datasourceId =
						Long.toString(ingestionDTO.getDatasourceId());

					String schemaName = ingestionDTO.getTenantId();

					List<BinaryDTO> binaries =
						ingestionDTO.getResources().getBinaries();

					List<Uni<BinaryDTO>> uploadUnis = new ArrayList<>();

					for (BinaryDTO binaryDTO : binaries) {

						try {

							byte[] data = binaryDTO.getData();

							String fileId = binaryDTO.getId();

							InputStream inputStream =
								new BufferedInputStream(
									new ByteArrayInputStream(data));

							var uploadUni = fileManagerClient.upload(
								datasourceId,
								fileId,
								schemaName,
								inputStream
							).map(resourceId -> {

								BinaryDTO newBinaryDTO = new BinaryDTO();
								newBinaryDTO.setId(fileId);
								newBinaryDTO.setName(binaryDTO.getName());
								newBinaryDTO.setContentType(
									binaryDTO.getContentType());
								newBinaryDTO.setResourceId(resourceId);

								return newBinaryDTO;
							}).invoke(newBinaryDTO -> {

								if (ingestionDTO.getResources().isSplitBinaries()) {

									IngestionDTO newIngestionDto =
										new IngestionDTO();

									ResourcesDTO newResourcesDTO =
										new ResourcesDTO();
									List<BinaryDTO> singeBinariesList =
										new ArrayList<>();
									singeBinariesList.add(newBinaryDTO);

									newResourcesDTO.setBinaries(
										singeBinariesList);

									newIngestionDto.setResources(
										newResourcesDTO);
									newIngestionDto.setContentId(fileId);
									newIngestionDto.setAcl(
										ingestionDTO.getAcl());
									newIngestionDto.setDatasourceId(
										ingestionDTO.getDatasourceId());
									newIngestionDto.setScheduleId(
										ingestionDTO.getScheduleId());
									newIngestionDto.setParsingDate(
										ingestionDTO.getParsingDate());
									newIngestionDto.setRawContent("");

									Map<String, Object> datasourcePayload =
										new HashMap<>();
									datasourcePayload.put(
										"file",
										new JsonObject()
									);
									newIngestionDto.setDatasourcePayload(
										datasourcePayload);

									emitter.emit(newIngestionDto);
								}
							});

							uploadUnis.add(uploadUni);

						} catch (NoSuchQueueException e) {
							throw e;
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						}

					}

					return Uni.join().all(uploadUnis)
						.usingConcurrencyOf(1)
						.andCollectFailures()
						.map(binaryDTOS -> {
							ResourcesDTO resourcesDTO = new ResourcesDTO();
							resourcesDTO.setBinaries(binaryDTOS);
							return resourcesDTO;
						});

				}

				return Uni.createFrom().item(ingestionDTO.getResources());
			})
			.invoke(resourcesDTO -> {
				ingestionDTO.setResources(resourcesDTO);
				emitter.emit(ingestionDTO);
			})
			.replaceWithVoid();

	}
}
