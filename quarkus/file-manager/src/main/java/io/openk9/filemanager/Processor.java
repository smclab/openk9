package io.openk9.filemanager;

import io.openk9.filemanager.service.UploadService;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.reactive.messaging.annotations.Blocking;
import io.smallrye.reactive.messaging.rabbitmq.OutgoingRabbitMQMetadata;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class Processor {

    @Incoming("file-manager")
    @Blocking
    public CompletionStage<Void> process(Message<?> message) {

        JsonObject jsonObject = _messagePayloadToJson(message);

        JsonObject response = jsonObject.copy();

        JsonObject payload = jsonObject.getJsonObject("payload");

        String datasourceId = payload.getString("datasourceId");

        JsonArray binaries =
                payload
                        .getJsonObject("resources")
                        .getJsonArray("binaries");

        if (!binaries.isEmpty()) {

            final int internalIndex = 0;

            JsonObject binaryJson =
                    binaries.getJsonObject(internalIndex);

            String data = binaryJson.getString("data");

            String fileId = binaryJson.getString("id");

            byte[] contentBytes = Base64.getDecoder().decode(data);

            InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(contentBytes));

            String resourceId = UUID.randomUUID().toString();

            uploadService.uploadObject(inputStream, datasourceId, fileId, resourceId);

            JsonObject responsePayload =
                    response.getJsonObject("payload");

            JsonObject resources =
                    responsePayload.getJsonObject("resources");


            JsonArray binariesArray =
                    resources.getJsonArray("binaries");
            binariesArray
                    .getJsonObject(internalIndex)
                    .put("resourceId", resourceId).put("data", null);


        }

        emitter.send(
                Message.of(
                        response,
                        Metadata.of(
                                OutgoingRabbitMQMetadata
                                        .builder()
                                        .withDeliveryMode(2)
                                        .build()
                        )
                ));

        return message.ack();
    }

    private JsonObject _messagePayloadToJson(Message<?> message) {
        Object obj = message.getPayload();

        return obj instanceof JsonObject
                ? (JsonObject) obj
                : new JsonObject(new String((byte[]) obj));

    }

    @Channel("file-manager-sender")
    Emitter<JsonObject> emitter;

    @Inject
    UploadService uploadService;

}