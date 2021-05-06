package io.openk9.plugin.driver.manager.ingestion;

import io.openk9.cbor.api.CBORFactory;
import io.openk9.ingestion.api.BundleReceiver;
import io.openk9.ingestion.api.BundleSender;
import io.openk9.model.IngestionDatasourcePayload;
import io.openk9.plugin.driver.manager.model.IngestionDatasourcePluginDriverPayload;
import io.openk9.plugin.driver.manager.api.PluginDriverDTOService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
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
				.map(delivery -> _cborFactory.fromCBOR(delivery.getBody(), IngestionDatasourcePayload.class))
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
				.transform(flux -> _bundleSender.send(flux.map(_cborFactory::toCBOR)))
				.subscribe();

	}

	@Deactivate
	void deactivate() {
		_disposable.dispose();
	}

	private Disposable _disposable;

	@Reference
	private CBORFactory _cborFactory;

	@Reference(target = "(queue=ingestion-datasource)")
	private BundleReceiver _bundleReceiver;

	@Reference(target = "(routingKey=io.openk9.ingestion.datasource.plugin-driver-manager)")
	private BundleSender _bundleSender;

	@Reference
	private PluginDriverDTOService _pluginDriverDTOService;

	private static final Logger _log =
		LoggerFactory.getLogger(IngestionDatasourcePubSub.class);

}
