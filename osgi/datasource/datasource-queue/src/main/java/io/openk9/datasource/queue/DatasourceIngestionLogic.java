package io.openk9.datasource.queue;

import io.openk9.cbor.api.CBORFactory;
import io.openk9.datasource.repository.DatasourceRepository;
import io.openk9.ingestion.api.BundleSender;
import io.openk9.model.IngestionDatasourcePayload;
import io.openk9.model.IngestionPayload;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component(
	immediate = true,
	service = DatasourceIngestionLogic.class
)
public class DatasourceIngestionLogic {

	@interface Config {
		long timespan() default 30_000;
	}

	@Activate
	void activate(Config config) {
		_disposable =
			_datasourceHotFlux
				.stream()
				.flatMap(ingestionPayload -> _datasourceRepository
					.findContext(ingestionPayload.getDatasourceId())
					.map(context ->
							IngestionDatasourcePayload.of(
								IngestionPayload.of(
									context.getDatasource().getDatasourceId(),
									ingestionPayload.getContentId(),
									ingestionPayload.getParsingDate(),
									ingestionPayload.getRawContent(),
									ingestionPayload.getDatasourcePayload(),
									context.getTenant().getTenantId(),
									ingestionPayload.getType()
								), context)
					)
					.map(_cborFactory::toCBOR)
					.transform(_bundleSender::send)
					.thenReturn(ingestionPayload))
				.groupBy(IngestionPayload::getDatasourceId)
				.flatMap(group -> group
					.sample(Duration.ofMillis(config.timespan())))
				.flatMap(ip -> _datasourceRepository
					.updateLastIngestionDate(
						ip.getDatasourceId(),
						Instant.ofEpochMilli(ip.getParsingDate()))
					.doOnNext(unused -> {
						if (_log.isDebugEnabled()) {
							_log.debug(
								"update lastIngestionDate " +
								new Date(ip.getParsingDate())
									.toInstant().toString() +
								"of the datasourceId: " +
								ip.getDatasourceId());
						}
					})
				)
				.subscribe();

	}

	@Modified
	void modified(Config config) {
		deactivate();
		activate(config);
	}

	@Deactivate
	void deactivate() {
		_disposable.dispose();
	}

	private Disposable _disposable;

	@Reference
	private CBORFactory _cborFactory;

	@Reference
	private DatasourceRepository _datasourceRepository;

	@Reference(target = "(routingKey=io.openk9.ingestion.datasource)")
	private BundleSender _bundleSender;

	@Reference
	private DatasourceHotFlux _datasourceHotFlux;

	private static final Logger _log =
		LoggerFactory.getLogger(DatasourceIngestionLogic.class);

}
