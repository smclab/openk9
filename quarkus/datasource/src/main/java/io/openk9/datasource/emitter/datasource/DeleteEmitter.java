package io.openk9.datasource.emitter.datasource;

import io.openk9.datasource.emitter.InternalEmitter;
import io.openk9.datasource.model.Datasource;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DeleteEmitter implements InternalEmitter<Datasource> {

	@Override
	public void send(Datasource datasource) {
		_deleteEmitter.send(datasource);
	}

	@Inject
	@Channel("datasource-DELETE-Datasource")
	Emitter<Datasource> _deleteEmitter;

}
