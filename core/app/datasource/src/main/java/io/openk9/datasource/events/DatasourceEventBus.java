package io.openk9.datasource.events;

import io.quarkus.runtime.Startup;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@Startup
public class DatasourceEventBus {

	public void sendEvent(DatasourceMessage datasourceMessage) {
		quoteRequestEmitter.send(datasourceMessage);
	}

	@Inject
	@Channel("datasource-events-requests")
	Emitter<DatasourceMessage> quoteRequestEmitter;

}
