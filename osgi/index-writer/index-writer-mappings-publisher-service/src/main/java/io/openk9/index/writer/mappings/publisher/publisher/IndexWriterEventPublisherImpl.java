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

package io.openk9.index.writer.mappings.publisher.publisher;

import io.openk9.index.writer.mappings.publisher.api.IndexWriterEventPublisher;
import io.openk9.index.writer.model.IndexTemplateDTO;
import io.openk9.ingestion.api.Binding;
import io.openk9.ingestion.api.OutboundMessageFactory;
import io.openk9.ingestion.api.SenderReactor;
import io.openk9.json.api.JsonFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
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

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private SenderReactor _senderReactor;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private OutboundMessageFactory _outboundMessageFactory;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private JsonFactory _jsonFactory;

	@Reference(
		target = "(component.name=io.openk9.index.writer.mappings.pub.sub.binding.MappingsBinding)",
		policyOption = ReferencePolicyOption.GREEDY
	)
	private Binding _binding;

	private static final Logger _log = LoggerFactory.getLogger(
		IndexWriterEventPublisherImpl.class);

}
