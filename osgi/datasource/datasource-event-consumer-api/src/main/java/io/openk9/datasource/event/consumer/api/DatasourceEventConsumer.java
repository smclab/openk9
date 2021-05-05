package io.openk9.datasource.event.consumer.api;

import io.openk9.model.Datasource;
import reactor.core.publisher.Flux;

public interface DatasourceEventConsumer {

	Flux<Datasource> datasourceUpdateEvents();

	Flux<Datasource> datasourceInsertEvents();

	Flux<Datasource> datasourceDeleteEvents();

}
