package io.openk9.index.writer.mappings.publisher.api;

import io.openk9.index.writer.model.IndexTemplateDTO;
import reactor.core.publisher.Mono;

public interface IndexWriterEventPublisher {

	Mono<Void> publishCreateIndexTemplate(IndexTemplateDTO indexTemplateDTO);

}
