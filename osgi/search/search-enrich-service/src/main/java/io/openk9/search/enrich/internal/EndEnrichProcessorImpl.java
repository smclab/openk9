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

package io.openk9.search.enrich.internal;

import io.openk9.cbor.api.CBORFactory;
import io.openk9.common.api.constant.Strings;
import io.openk9.ingestion.api.OutboundMessageFactory;
import io.openk9.ingestion.api.SenderReactor;
import io.openk9.search.enrich.api.EndEnrichProcessor;
import io.openk9.search.enrich.api.dto.EnrichProcessorContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import reactor.core.publisher.Mono;

@Component(
	immediate = true,
	service = EndEnrichProcessor.class
)
public class EndEnrichProcessorImpl implements EndEnrichProcessor {

	@interface Config {
		String exchange() default "index-writer.topic";
		String routingKeySuffix() default "data";
	}

	@Activate
	void activate(Config config) {
		_exchange = config.exchange();
		_routingKeySuffix = config.routingKeySuffix();
	}

	@Modified
	void modified(Config config) {
		activate(config);
	}

	@Override
	public String name() {
		return EndEnrichProcessorImpl.class.getName();
	}

	@Override
	public Mono<Void> exec(
		EnrichProcessorContext enrichProcessorContext) {

		return _senderReactor.sendMono(
			Mono.fromSupplier(() -> {

				Long tenantId =
					enrichProcessorContext
						.getDatasourceContext()
						.getTenant()
						.getTenantId();

				String pluginDriverName =
					enrichProcessorContext.getPluginDriverDTO().getName();

				return _outboundMessageFactory.createOutboundMessage(
						builder -> builder
							.exchange(_exchange)
							.routingKey(
								String.join(
									Strings.DASH,
									tenantId.toString(),
									pluginDriverName,
									_routingKeySuffix))
							.body(_cborFactory.toCBOR(enrichProcessorContext))
					);
				}
			)
		);

	}

	private String _routingKeySuffix;
	private String _exchange;

	@Reference
	private SenderReactor _senderReactor;

	@Reference
	private CBORFactory _cborFactory;

	@Reference
	private OutboundMessageFactory _outboundMessageFactory;

}
