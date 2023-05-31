package io.openk9.datasource.events;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DatasourceEventBus {

	public void sendEvent(DatasourceEvent datasourceEvent) {
		quoteRequestEmitter.send(datasourceEvent);
	}

	@Channel("datasource-events")
	Emitter<DatasourceEvent> quoteRequestEmitter;


}
