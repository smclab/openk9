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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.common.storage.BinaryKeys;
import io.openk9.ingestion.dto.BinaryDTO;
import io.openk9.ingestion.dto.IngestionDTO;
import io.openk9.ingestion.dto.ResourcesDTO;
import io.openk9.ingestion.exception.NoSuchQueueException;
import io.openk9.ingestion.storage.BinaryStorageService;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import org.jboss.logging.Logger;

/**
 * Writes the binaries of an ingestion request to the object storage under
 * their deterministic key, strips the raw bytes from the payload and emits the
 * reference-only payload (fileId + contentType) on the ingestion queue.
 */
@ApplicationScoped
public class BinaryStorageEmitter {

	@Inject
	IngestionEmitter emitter;
	@Inject
	BinaryStorageService binaryStorageService;
	@Inject
	Logger logger;

	public Uni<Void> emit(IngestionDTO ingestionDTO) {

		return Uni.createFrom()
			.deferred(() -> {

				if (ingestionDTO.getResources() != null
					&& ingestionDTO.getResources().getBinaries() != null
					&& !ingestionDTO.getResources().getBinaries().isEmpty()) {

					logger.debugf(
						"Handling %d binaries for content %s",
						ingestionDTO.getResources().getBinaries().size(),
						ingestionDTO.getContentId());

					long datasourceId = ingestionDTO.getDatasourceId();

					String tenantId = ingestionDTO.getTenantId();

					boolean splitBinaries =
						ingestionDTO.getResources().isSplitBinaries();

					List<BinaryDTO> binaries =
						ingestionDTO.getResources().getBinaries();

					List<Uni<BinaryDTO>> uploadUnis = new ArrayList<>();

					for (BinaryDTO binaryDTO : binaries) {

						try {

							byte[] data = binaryDTO.getData();

							String fileId = binaryDTO.getId();

							if (data.length > 0) {

								String contentId = splitBinaries
									? fileId
									: ingestionDTO.getContentId();

								String key = BinaryKeys.key(
									datasourceId, contentId, fileId);

								BinaryDTO reference = BinaryDTO
									.builder()
									.id(fileId)
									.name(binaryDTO.getName())
									.contentType(binaryDTO.getContentType())
									.build();

								var uploadUni = binaryStorageService.store(
									tenantId,
									key,
									data,
									binaryDTO.getContentType()
								).replaceWith(
									reference
								).invoke(uploadedBinaryDTO -> {

									if (splitBinaries) {

										IngestionDTO newIngestionDto =
											new IngestionDTO();

										ResourcesDTO newResourcesDTO =
											new ResourcesDTO();
										List<BinaryDTO> singeBinariesList =
											new ArrayList<>();
										singeBinariesList.add(uploadedBinaryDTO);

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
							}

						}
						catch (NoSuchQueueException e) {
							throw e;
						}
						catch (Exception e) {
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
