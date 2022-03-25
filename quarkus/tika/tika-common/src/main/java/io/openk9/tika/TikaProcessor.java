package io.openk9.tika;

import io.openk9.tika.api.Message;
import io.openk9.tika.api.OutgoingMessage;
import io.openk9.tika.api.Publisher;
import io.openk9.tika.config.TikaConfiguration;
import io.openk9.tika.util.Detectors;
import io.quarkus.runtime.Startup;
import io.quarkus.tika.TikaContent;
import io.quarkus.tika.TikaMetadata;
import io.quarkus.tika.TikaParser;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.Cancellable;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.mime.MediaType;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@ApplicationScoped
@Startup
public class TikaProcessor {

    @PostConstruct
    void activate() {

        _executorService = Executors.newSingleThreadExecutor(
            _newThreadFacory(_THREAD_NAME)
        );

        _cancellable = tikaStream
            .emitOn(_executorService)
            .onItem()
            .invoke(message -> {

                String messageJson = new String(message.body());

                JsonObject jsonObject = new JsonObject(messageJson);

                String replyTo = jsonObject.getString("replyTo");

                JsonObject payload = jsonObject.getJsonObject("payload");

                JsonArray binaries =
                    payload
                        .getJsonObject("resources")
                        .getJsonArray("binaries");

                logger.info(
                    "dequeue message: " +
                    StringUtils.abbreviate(messageJson, 25));

                if (!binaries.isEmpty()) {

                    final int internalIndex = 0;

                    JsonObject enrichItemConfig =
                        jsonObject.getJsonObject("enrichItemConfig");

                    Integer maxLength =
                        enrichItemConfig.getInteger("max_length", -1);

                    JsonObject typeMapping =
                        enrichItemConfig.getJsonObject("type_mapping");

                    JsonObject response = jsonObject.copy();

                    JsonObject binaryJson =
                        binaries.getJsonObject(internalIndex);

                    String data = binaryJson.getString("data");

                    String name = binaryJson.getString("name");

                    byte[] contentBytes = Base64.getDecoder().decode(data);

                    try {

                        MediaType mediaType = _detectors.detect(
                            new BufferedInputStream(
                                new ByteArrayInputStream(contentBytes))
                        );

                        String metaTypeString = mediaType.toString();

                        if (typeMapping != null &&
                            typeMapping.containsKey(metaTypeString)) {

                            String typeMappingValue =
                                typeMapping.getString(metaTypeString);

                            Instant start = Instant.now();

                            TikaContent tikaContent = _tikaParser.parse(
                                new BufferedInputStream(
                                    new ByteArrayInputStream(contentBytes)
                                )
                            );

                            Duration end =
                                Duration.between(start, Instant.now());

                            logger.info("duration: " + end + " name: " + name);

                            TikaMetadata metadata = tikaContent.getMetadata();

                            String lastModified =
                                metadata.getSingleValue("Last-Modified");

                            String xParsedBy =
                                metadata.getSingleValue("X-Parsed-By");

                            if (xParsedBy != null) {
                                logger.info(
                                    "document parsed by: " + xParsedBy +
                                    " name: " + name);
                            }

                            JsonObject responsePayload =
                                response.getJsonObject("payload");

                            JsonArray documentTypes =
                                responsePayload.getJsonArray("documentTypes");

                            if (documentTypes == null) {
                                documentTypes = new JsonArray();
                                responsePayload.put(
                                    "documentTypes", documentTypes);
                            }

                            documentTypes.add(typeMappingValue);

                            if (lastModified != null) {

                                JsonObject file =
                                    responsePayload.getJsonObject("file");

                                if (file != null) {
                                    file.put("lastModifiedDate", lastModified);
                                }
                                else {
                                    responsePayload
                                        .put(
                                            "file",
                                            new JsonObject()
                                                .put(
                                                    "lastModifiedDate",
                                                    lastModified));
                                }
                            }

                            String contentType =
                                metadata.getSingleValue(
                                    HttpHeaders.CONTENT_TYPE);

                            JsonObject document =
                                responsePayload.getJsonObject("document");

                            if (document == null) {
                                document = new JsonObject();
                                responsePayload.put("document", document);
                            }

                            if (contentType != null) {
                                document.put("contentType", contentType);
                            }

                            String text = tikaContent.getText();

                            if (tikaConfiguration.isOcrEnabled()) {

                                if (text.length() <
                                    tikaConfiguration.getCharacterLength()) {
                                    processor.publish(
                                        OutgoingMessage.of(
                                            tikaConfiguration.getOcrExchange(),
                                            tikaConfiguration.getOcrRoutingKey(),
                                            jsonObject.toString().getBytes()
                                        )
                                    );
                                    return;
                                }

                            }

                            text = text.replaceAll("\\s+", " ");

                            document.put("content", text);

                            if (text.length() > maxLength) {

                                responsePayload.put(
                                    "rawContent", text.substring(0, maxLength));

                            }
                            else {
                                responsePayload.put("rawContent", text);
                            }

                            JsonObject resources =
                                responsePayload.getJsonObject("resources");

                            JsonArray binariesArray =
                                resources.getJsonArray("binaries");

                            if (binariesArray == null) {
                                binariesArray = new JsonArray();
                                resources.put("binaries", binariesArray);
                            }

                            binariesArray
                                .getJsonObject(internalIndex)
                                .put("contentType", contentType);

                            processor.publish(
                                OutgoingMessage.of(
                                    "amq.topic",
                                    replyTo,
                                    response.toString().getBytes()
                                )
                            );
                            return;

                        }

                    }
                    catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }

                    processor.publish(
                        OutgoingMessage.of(
                            "amq.topic",
                            replyTo,
                            jsonObject.toString().getBytes()
                        )
                    );

                }

            })
            .call(Message::ack)
            .subscribe()
            .with(message -> {});
    }

    @PreDestroy
    public void stop() {
        _cancellable.cancel();
        _executorService.shutdown();
    }

    private static ThreadFactory _newThreadFacory(String name) {

        return r -> {

            Thread thread = new Thread(r, name);

            thread.setDaemon(true);

            ClassLoader classLoader =
                Thread.currentThread().getContextClassLoader();

            if (classLoader != null) {
                thread.setContextClassLoader(classLoader);
            }

            return thread;
        };

    }

    private Cancellable _cancellable;

    private ExecutorService _executorService;

    private static final String _THREAD_NAME = "tika-thread";

    @Inject
    @Named("io.openk9.tika")
    Multi<Message> tikaStream;

    @Inject
    Publisher processor;

    @Inject
    TikaParser _tikaParser;

    @Inject
    Detectors _detectors;

    @Inject
    Logger logger;

    @Inject
    TikaConfiguration tikaConfiguration;

}
