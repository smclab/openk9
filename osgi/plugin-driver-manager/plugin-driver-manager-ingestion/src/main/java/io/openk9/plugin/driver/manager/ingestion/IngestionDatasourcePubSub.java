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

package io.openk9.plugin.driver.manager.ingestion;

import io.openk9.ingestion.api.BundleReceiver;
import io.openk9.ingestion.api.BundleSender;
import io.openk9.json.api.JsonFactory;
import io.openk9.model.IngestionDatasourcePayload;
import io.openk9.plugin.driver.manager.api.PluginDriverDTOService;
import io.openk9.plugin.driver.manager.model.IngestionDatasourcePluginDriverPayload;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

@Component(
	immediate = true,
	service = IngestionDatasourcePubSub.class
)
public class IngestionDatasourcePubSub {

	@Activate
	void activate() {
		_disposable =
			_bundleReceiver
				.consumeAutoAck()
				.map(delivery -> _jsonFactory.fromJson(delivery.getBody(), IngestionDatasourcePayload.class))
				.map(ingestionDatasourcePayload ->
					_pluginDriverDTOService
						.findPluginDriverDTOByName(
							ingestionDatasourcePayload
								.getDatasourceContext()
								.getDatasource()
								.getDriverServiceName())
					.map(pluginDriverDTO ->
						IngestionDatasourcePluginDriverPayload.of(
							ingestionDatasourcePayload.getIngestionPayload(),
							ingestionDatasourcePayload.getDatasourceContext(),
							pluginDriverDTO
						)
					)
				)
				.flatMap(Mono::justOrEmpty)
				.transform(flux -> _bundleSender.send(flux.map(_jsonFactory::toJson).map(String::getBytes)))
				.subscribe();

	}

	@Deactivate
	void deactivate() {
		_disposable.dispose();
	}

	private Disposable _disposable;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private JsonFactory _jsonFactory;

	@Reference(
		target = "(queue=ingestion-datasource)",
		policyOption = ReferencePolicyOption.GREEDY
	)
	private BundleReceiver _bundleReceiver;

	@Reference(
		target = "(routingKey=io.openk9.ingestion.datasource.plugin-driver-manager)",
		policyOption = ReferencePolicyOption.GREEDY
	)
	private BundleSender _bundleSender;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private PluginDriverDTOService _pluginDriverDTOService;

	private static final Logger _log =
		LoggerFactory.getLogger(IngestionDatasourcePubSub.class);

}
