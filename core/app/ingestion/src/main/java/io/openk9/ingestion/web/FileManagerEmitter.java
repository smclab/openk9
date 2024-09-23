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
import io.smallrye.mutiny.operators.multi.processors.UnicastProcessor;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.PostConstruct;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class FileManagerEmitter {

    private UnicastProcessor<IngestionDTO> processor = UnicastProcessor.create();

    public CompletionStage<Void> emit(IngestionDTO ingestionDTO) {

        processor.onNext(ingestionDTO);

        return CompletableFuture.completedFuture(null);
    }

    @PostConstruct
    private void init() {

        processor.subscribe().with(ingestionDTO -> {

            if (!(ingestionDTO.getResources() == null)) {

                if (!ingestionDTO.getResources().getBinaries().isEmpty()) {


                    logger.info("Handling binaries");

                    String datasourceId =
                        Long.toString(ingestionDTO.getDatasourceId());

                    String schemaName = ingestionDTO.getTenantId();

                    List<BinaryDTO> binaries =
                        ingestionDTO.getResources().getBinaries();

                    List<BinaryDTO> modifiedBinaries = new ArrayList<>();

                    for (BinaryDTO binaryDTO : binaries) {

                        try {

                            String data = binaryDTO.getData();

                            String fileId = binaryDTO.getId();

                            if (data.length() > 0) {

                                byte[] contentBytes =
                                    Base64.getDecoder().decode(data);

                                InputStream inputStream =
                                    new BufferedInputStream(
                                        new ByteArrayInputStream(contentBytes));

                                String resourceId =
                                    fileManagerClient.upload(
                                        datasourceId,
                                        fileId,
                                        schemaName,
                                        inputStream
                                    );

                                BinaryDTO newBinaryDTO = new BinaryDTO();
                                newBinaryDTO.setId(fileId);
                                newBinaryDTO.setData(null);
                                newBinaryDTO.setName(binaryDTO.getName());
                                newBinaryDTO.setContentType(
                                    binaryDTO.getContentType());
                                newBinaryDTO.setResourceId(resourceId);

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
                                        new JsonObject());
                                    newIngestionDto.setDatasourcePayload(
                                        datasourcePayload);

                                    emitter.emit(newIngestionDto);
                                }
                                else {
                                    modifiedBinaries.add(newBinaryDTO);
                                }
                            }
                        }
                        catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }

                    }

                    ResourcesDTO resourcesDTO = new ResourcesDTO();
                    resourcesDTO.setBinaries(modifiedBinaries);
                    ingestionDTO.setResources(resourcesDTO);


                }

            }

            emitter.emit(ingestionDTO);

        });
    }

    @Inject
    IngestionEmitter emitter;

    @Inject
    @RestClient
    FileManagerClient fileManagerClient;

    @Inject
    Logger logger;
}
