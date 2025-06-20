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

package io.openk9.tika;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.tika.client.DatasourceClient;
import io.openk9.tika.client.FileManagerClient;
import io.openk9.tika.util.Detectors;
import io.openk9.tika.util.TextCleaner;

import io.quarkus.tika.TikaContent;
import io.quarkus.tika.TikaMetadata;
import io.quarkus.tika.TikaParser;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.mime.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TikaProcessor {

    public void process(JsonObject jsonObject) {

        long startTime = System.currentTimeMillis();

        JsonObject payload = jsonObject.getJsonObject("payload");

        String replyTo = jsonObject.getString("replyTo");

        String schemaName = payload.getString("tenantId");

        JsonArray binaries =
            payload
                .getJsonObject("resources")
                .getJsonArray("binaries");

        if (!binaries.isEmpty()) {

            final int internalIndex = 0;

            JsonObject enrichItemConfig =
                jsonObject.getJsonObject("enrichItemConfig");

            Integer maxLength =
                enrichItemConfig.getInteger("max_length", -1);

            Integer summaryLength =
                enrichItemConfig.getInteger("summary_length", -1);

            JsonObject typeMapping =
                enrichItemConfig.getJsonObject("type_mapping");

            JsonObject response = jsonObject.copy();

            JsonObject binaryJson =
                binaries.getJsonObject(internalIndex);

            String resourceId = binaryJson.getString("resourceId");

            String name = binaryJson.getString("name");

            logger.info("Processing resource with id: " + resourceId + " and name: " + name);

            boolean retainBinaries = enrichItemConfig.getBoolean("retain_binaries");

            try (InputStream inputStream = new BufferedInputStream(fileManagerClient.download(resourceId, schemaName))) {

                MediaType mediaType = _detectors.detect(inputStream);

                String metaTypeString = mediaType.toString();

                logger.info("Detected type : " + metaTypeString + " for file: " + name);

                if (typeMapping != null &&
                        typeMapping.containsKey(metaTypeString)) {

                    String typeMappingValue =
                            typeMapping.getString(metaTypeString);

                    Instant start = Instant.now();

                    TikaContent tikaContent = _tikaParser.parse(inputStream);

                    Duration end =
                            Duration.between(start, Instant.now());

                    logger.info("duration: " + end + " name: " + name);

                    TikaMetadata metadata = tikaContent.getMetadata();

                    String lastModified =
                            metadata.getSingleValue("Last-Modified");

                    String xParsedBy =
                            metadata.getSingleValue("X-Parsed-By");

                    String title =
                        metadata.getSingleValue("title");

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

                    Boolean includeLastModified =
                            enrichItemConfig.getBoolean("include_last_modified_date", true);

                    if (lastModified != null && includeLastModified) {

                        JsonObject file =
                                responsePayload.getJsonObject("file");

                        if (file != null) {
                            file.put("lastModifiedDate", lastModified);
                        } else {
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
                        documentTypes.add("document");
                    }

                    Boolean includeContentType =
                            enrichItemConfig.getBoolean("include_content_type", true);

                    if (contentType != null && includeContentType) {
                        document.put("contentType", contentType);
                    }

                    String text = tikaContent.getText();

                    text = TextCleaner.cleanText(text);

                    if (text.length() > maxLength && maxLength > 0) {
                        text = text.substring(0, maxLength);
                    }

                    document.put("content", text);
                    responsePayload.put("rawContent", text);

                    logger.info("Cleaned text parsed from " + name);

                    if (text.length() > summaryLength && summaryLength > 0) {

                        document.put("summary", text.substring(0, summaryLength));
                    } else {
                        document.put("summary", text);
                    }

                    if (document.getString("title") == null) {
                        document.put("title", title);
                    }

                    JsonObject resources =
                            responsePayload.getJsonObject("resources");

                    JsonArray binariesArray =
                            resources.getJsonArray("binaries");
                    binariesArray
                            .getJsonObject(internalIndex)
                            .put("contentType", contentType);

                    if (!retainBinaries) {
                        logger.info("Deleting resource with id: " + resourceId + " and name: " + name);
                        fileManagerClient.delete(resourceId, schemaName);
                    }

                    logger.debug(response.toString());

                    datasourceClient.sentToPipeline(replyTo, response.toString());

                    logger.info("Send message to datasource with token: " + replyTo);

                    long estimatedTime = System.currentTimeMillis() - startTime;
                    logger.info(estimatedTime);

                    return;

                }
                else {
                    if (!retainBinaries) {
                        fileManagerClient.delete(resourceId, schemaName);
                        logger.info("Skipping resource with id: " + resourceId + " and name: " + name
                        + " because not supported by configuration");
                    }
                }

            } catch (Exception e) {

                logger.error(e.getMessage(), e);

                throw new RuntimeException();
            }

        }

        datasourceClient.sentToPipeline(replyTo, jsonObject.toString());

        logger.info("Send message to datasource with token: " + replyTo);

        long estimatedTime = System.currentTimeMillis() - startTime;
        logger.info(estimatedTime);

    }

    @Inject
    TikaParser _tikaParser;

    @Inject
    Detectors _detectors;

    @Inject
    Logger logger;

    @Inject
    @RestClient
    FileManagerClient fileManagerClient;

    @Inject
    @RestClient
    DatasourceClient datasourceClient;

}
