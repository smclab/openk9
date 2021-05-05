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

package io.openk9.datasource.event.publisher;

import io.openk9.datasource.event.configuration.DatasourceEventConfiguration;
import io.openk9.ingestion.api.Binding;
import io.openk9.ingestion.api.BindingRegistry;
import io.openk9.ingestion.api.OutboundMessage;
import io.openk9.ingestion.api.OutboundMessageFactory;
import io.openk9.ingestion.api.SenderReactor;
import io.openk9.json.api.JsonFactory;
import io.openk9.osgi.util.AutoCloseables;
import io.openk9.sql.api.event.EntityEvent;
import io.openk9.sql.api.event.EntityEventBus;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

@Component(
	immediate = true,
	service = {
		DatasourceEventPublisher.class
	}
)
@Designate(ocd = DatasourceEventConfiguration.class)
public class DatasourceEventPublisher {

	@Activate
	public void activate(DatasourceEventConfiguration datasourceEventConfiguration) {

		_routingKeyTemplate = datasourceEventConfiguration.routingKeyTemplate();

		_exchange = datasourceEventConfiguration.exchange();

		_bindingClosable = _bindingRegistry.register(
			Binding.Exchange.of(_exchange, Binding.Exchange.Type.direct)
		);

		_disposable = _entityEventBus
			.stream()
			.flatMap(this::_publishEvent)
			.subscribe();

	}

	@Modified
	public void modified(DatasourceEventConfiguration config) {
		deactivate();
		activate(config);
	}

	@Deactivate
	public void deactivate() {
		_bindingClosable.close();
		_disposable.dispose();
	}

	private Publisher<Void> _publishEvent(EntityEvent<?> entityEvent) {

		return Mono.defer(() -> {

			String entityName = entityEvent.getEntityClass().getSimpleName();

			String eventType = entityEvent.getType().name();

			String routingKey = _createRoutingKey(
				_routingKeyTemplate, eventType, entityName);

			String bodyJson = _jsonFactory.toJson(entityEvent.getValue());

			if (_log.isDebugEnabled()) {
				_log.debug(
					"message published. " +
					"exchange: " + _exchange + " " +
					"routingKey: " + routingKey + " " +
					"message: " + bodyJson
				);
			}

			OutboundMessage outboundMessage =
				_outboundMessageFactory.createOutboundMessage(
					builder -> builder
						.exchange(_exchange)
						.routingKey(routingKey)
						.body(bodyJson.getBytes())
				);

			return _senderReactor.sendMono(Mono.just(outboundMessage));

		});

	}

	private static String _createRoutingKey(
		String template, String eventType, String entityName) {

		return template
			.replace("{eventType}", eventType)
			.replace("{entityName}", entityName);

	}

	private String _exchange;

	private String _routingKeyTemplate;

	private Disposable _disposable;

	private AutoCloseables.AutoCloseableSafe _bindingClosable;

	@Reference
	private JsonFactory _jsonFactory;

	@Reference
	private SenderReactor _senderReactor;

	@Reference
	private EntityEventBus _entityEventBus;

	@Reference
	private BindingRegistry _bindingRegistry;

	@Reference
	private OutboundMessageFactory _outboundMessageFactory;

	private static final Logger _log = LoggerFactory.getLogger(
		DatasourceEventPublisher.class);

}
