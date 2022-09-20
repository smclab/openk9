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

import io.openk9.tika.client.filemanager.FileManagerClient;
import io.openk9.tika.util.Detectors;
import io.quarkus.tika.TikaContent;
import io.quarkus.tika.TikaMetadata;
import io.quarkus.tika.TikaParser;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.tika.metadata.HttpHeaders;
import org.apache.tika.mime.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@ApplicationScoped
public class TikaProcessor {

    public Tuple2<String, JsonObject> process(
        JsonObject jsonObject, boolean isOcr, int characterLength,
        String ocrReplyTo) {

        JsonObject payload = jsonObject.getJsonObject("payload");

        String replyTo = jsonObject.getString("replyTo");

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

            InputStream inputStream = fileManagerClient.download(resourceId);

            try {

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

                    Boolean includeContentType =
                        enrichItemConfig.getBoolean("include_content_type", true);

                    if (contentType != null && includeContentType) {
                        document.put("contentType", contentType);
                    }

                    String text = tikaContent.getText();

                    if (isOcr) {

                        if (text.length() < characterLength) {
                            return Tuple2.of(ocrReplyTo, jsonObject);
                        }

                    }

                    text = text.replaceAll("\\s+", " ");

                    document.put("content", text);

                    if (text.length() > summaryLength && summaryLength > 0) {

                        document.put("summary", text.substring(0, summaryLength));
                    }
                    else {
                        document.put("summary", text);
                    }

                    text = text.replaceAll("\\n", " ");
                    text = text.replaceAll("\\t", " ");

                    if (text.length() > maxLength && maxLength > 0) {

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
                    binariesArray
                        .getJsonObject(internalIndex)
                        .put("contentType", contentType);

                    return Tuple2.of(replyTo, response);

                }

            }
            catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        }

        return Tuple2.of(replyTo, jsonObject);

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

}
