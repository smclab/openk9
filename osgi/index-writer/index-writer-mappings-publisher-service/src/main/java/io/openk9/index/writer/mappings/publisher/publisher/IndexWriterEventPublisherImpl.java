package io.openk9.index.writer.mappings.publisher.publisher;

import io.openk9.index.writer.mappings.publisher.api.IndexWriterEventPublisher;
import io.openk9.index.writer.model.IndexTemplateDTO;
import io.openk9.ingestion.api.Binding;
import io.openk9.ingestion.api.OutboundMessageFactory;
import io.openk9.ingestion.api.SenderReactor;
import io.openk9.json.api.JsonFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

@Component(
	immediate = true,
	service = IndexWriterEventPublisher.class
)
public class IndexWriterEventPublisherImpl implements
	IndexWriterEventPublisher {

	@Override
	public Mono<Void> publishCreateIndexTemplate(
		IndexTemplateDTO indexTemplateDTO) {

		return Mono.defer(() -> {

			String json = _jsonFactory.toJson(indexTemplateDTO);

			if (_log.isDebugEnabled()) {
				_log.debug("published: " + json);
			}

			return _senderReactor.sendMono(
				Mono.just(
					_outboundMessageFactory
						.createOutboundMessage(
							builder ->
								builder
									.exchange("index-writer-mappings.fanout")
									.routingKey("#")
									.body(json.getBytes())
						)
				)
			);
		});

	}

	@Reference
	private SenderReactor _senderReactor;

	@Reference
	private OutboundMessageFactory _outboundMessageFactory;

	@Reference
	private JsonFactory _jsonFactory;

	@Reference(target = "(component.name=io.openk9.index.writer.mappings.pub.sub.binding.MappingsBinding)")
	private Binding _binding;

	private static final Logger _log = LoggerFactory.getLogger(
		IndexWriterEventPublisherImpl.class);

}
