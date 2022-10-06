package io.openk9.ingestion.web;

import io.openk9.ingestion.client.filemanager.FileManagerClient;
import io.openk9.ingestion.dto.BinaryDTO;
import io.openk9.ingestion.dto.IngestionDTO;
import io.openk9.ingestion.dto.IngestionPayload;
import io.openk9.ingestion.dto.ResourcesDTO;
import io.smallrye.mutiny.operators.multi.processors.UnicastProcessor;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.openapi.models.parameters.Parameter;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
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

            if (!ingestionDTO.getResources().getBinaries().isEmpty()) {

                logger.info("Handling binaries");

                String datasourceId = Long.toString(ingestionDTO.getDatasourceId());

                List<BinaryDTO> binaries = ingestionDTO.getResources().getBinaries();

                List<BinaryDTO> modifiedBinaries = new ArrayList<>();

                for (BinaryDTO binaryDTO : binaries) {

                    String data = binaryDTO.getData();

                    String fileId = binaryDTO.getId();

                    if (data.length() > 0) {

                        byte[] contentBytes = Base64.getDecoder().decode(data);

                        InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(contentBytes));

                        String resourceId = fileManagerClient.upload(datasourceId, fileId, inputStream);

                        BinaryDTO newBinaryDTO = new BinaryDTO();
                        newBinaryDTO.setId(fileId);
                        newBinaryDTO.setData(null);
                        newBinaryDTO.setName(binaryDTO.getName());
                        newBinaryDTO.setContentType(binaryDTO.getContentType());
                        newBinaryDTO.setResourceId(resourceId);

                        if (ingestionDTO.getResources().isSplitBinaries()) {

                            IngestionDTO newIngestionDto = new IngestionDTO();

                            ResourcesDTO newResourcesDTO = new ResourcesDTO();
                            List<BinaryDTO> singeBinariesList = new ArrayList<>();
                            singeBinariesList.add(newBinaryDTO);

                            newResourcesDTO.setBinaries(singeBinariesList);

                            newIngestionDto.setResources(newResourcesDTO);
                            newIngestionDto.setContentId(fileId);
                            newIngestionDto.setAcl(ingestionDTO.getAcl());
                            newIngestionDto.setDatasourceId(ingestionDTO.getDatasourceId());
                            newIngestionDto.setScheduleId(ingestionDTO.getScheduleId());
                            newIngestionDto.setParsingDate(ingestionDTO.getParsingDate());
                            newIngestionDto.setRawContent("");
                            newIngestionDto.setDatasourcePayload(new HashMap<>());

                            logger.info(newIngestionDto.toString());

                            emitter.emit(newIngestionDto);
                        }
                        else {
                            modifiedBinaries.add(newBinaryDTO);
                        }
                    }
                }

                ResourcesDTO resourcesDTO = new ResourcesDTO();
                resourcesDTO.setBinaries(modifiedBinaries);
                ingestionDTO.setResources(resourcesDTO);

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
