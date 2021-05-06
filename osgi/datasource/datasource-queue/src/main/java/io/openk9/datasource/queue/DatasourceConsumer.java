package io.openk9.datasource.queue;

import io.openk9.cbor.api.CBORFactory;
import io.openk9.ingestion.api.BundleReceiver;
import io.openk9.model.IngestionPayload;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import reactor.core.Disposable;

@Component(
	immediate = true,
	service = DatasourceConsumer.class
)
public class DatasourceConsumer {

	@Activate
	void activate() {

		_disposable = _bundleReceiver
			.consumeAutoAck()
			.map(d -> _cborFactory.fromCBOR(d.getBody(), IngestionPayload.class))
			.doOnNext(_datasourceHotFlux::send)
			.subscribe();

	}

	@Deactivate
	void deactivate() {
		_disposable.dispose();
	}

	private Disposable _disposable;

	@Reference
	private CBORFactory _cborFactory;

	@Reference(target = "(queue=ingestion)")
	private BundleReceiver _bundleReceiver;

	@Reference
	private DatasourceHotFlux _datasourceHotFlux;

}
