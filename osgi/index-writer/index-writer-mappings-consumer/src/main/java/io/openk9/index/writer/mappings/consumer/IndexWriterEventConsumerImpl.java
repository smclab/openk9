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

package io.openk9.index.writer.mappings.consumer;

import io.openk9.index.writer.model.IndexTemplateDTO;
import io.openk9.ingestion.api.Binding;
import io.openk9.ingestion.api.ReceiverReactor;
import io.openk9.json.api.JsonFactory;
import io.openk9.search.client.api.indextemplate.IndexTemplateService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;

@Component(
	immediate = true,
	service = IndexWriterEventConsumerImpl.class
)
public class IndexWriterEventConsumerImpl {

	@Activate
	void activate() {
		_disposable =
			_receiverReactor
				.consumeAutoAck("index-writer-mappings")
				.map(delivery -> _jsonFactory.fromJson(delivery.getBody(), IndexTemplateDTO.class))
				.doOnNext(this::_logInfo)
				.subscribe(IndexTemplateDTO ->
					_indexTemplateService.createOrUpdateIndexTemplate(
						IndexTemplateDTO.getIndexTemplateName(),
						IndexTemplateDTO.getSettings(),
						IndexTemplateDTO.getIndexPatterns(),
						IndexTemplateDTO.getMappings(),
						IndexTemplateDTO.getComponentTemplates(),
						IndexTemplateDTO.getPriority()
					)
				);
	}

	@Deactivate
	void deactivate() {
		_disposable.dispose();
	}

	private void _logInfo(IndexTemplateDTO indexTemplateDTO) {
		if (_log.isInfoEnabled()) {
			_log.info("consuming: " + indexTemplateDTO);
		}
	}

	private Disposable _disposable;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private ReceiverReactor _receiverReactor;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private JsonFactory _jsonFactory;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private IndexTemplateService _indexTemplateService;

	@Reference(
		target = "(component.name=io.openk9.index.writer.mappings.pub.sub.binding.MappingsBinding)",
		policyOption = ReferencePolicyOption.GREEDY
	)
	private Binding _binding;

	private static final Logger _log = LoggerFactory.getLogger(
		IndexWriterEventConsumerImpl.class);

}
