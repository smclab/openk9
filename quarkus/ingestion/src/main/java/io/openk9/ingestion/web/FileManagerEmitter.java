package io.openk9.ingestion.web;

import io.openk9.ingestion.client.filemanager.FileManagerClient;
import io.openk9.ingestion.dto.BinaryDTO;
import io.openk9.ingestion.dto.IngestionDTO;
import io.openk9.ingestion.dto.IngestionPayload;
import io.openk9.ingestion.dto.ResourcesDTO;
import io.smallrye.mutiny.operators.multi.processors.UnicastProcessor;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
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

                final int internalIndex = 0;

                BinaryDTO binary = binaries.get(internalIndex);

                String data = binary.getData();

                String fileId = binary.getId();

                byte[] contentBytes = Base64.getDecoder().decode(data);

                InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(contentBytes));

                String resourceId = fileManagerClient.upload(datasourceId, fileId, inputStream);

                BinaryDTO newBinaryDTO = new BinaryDTO();
                newBinaryDTO.setId(fileId);
                newBinaryDTO.setData(null);
                newBinaryDTO.setName(binary.getName());
                newBinaryDTO.setContentType(binary.getContentType());
                newBinaryDTO.setResourceId(resourceId);

                List<BinaryDTO> modifiedBinaries = new ArrayList<>();
                modifiedBinaries.add(newBinaryDTO);

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
