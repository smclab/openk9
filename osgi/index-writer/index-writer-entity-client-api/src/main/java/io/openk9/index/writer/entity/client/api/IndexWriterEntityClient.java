package io.openk9.index.writer.entity.client.api;

import io.openk9.index.writer.entity.model.DocumentEntityRequest;
import io.openk9.index.writer.entity.model.DocumentEntityResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface IndexWriterEntityClient {

	Mono<Void> insertEntity(DocumentEntityRequest documentEntityRequest);

	Mono<List<DocumentEntityResponse>> getEntities(Map<String, Object> request);

}
