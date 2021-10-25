package io.openk9.datasource.emitter.datasource;

import io.openk9.datasource.emitter.InternalEmitter;
import io.openk9.datasource.model.Datasource;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class InsertEmitter implements InternalEmitter<Datasource> {

	@Override
	public void send(Datasource datasource) {
		_insertEmitter.send(datasource);
	}

	@Inject
	@Channel("datasource-INSERT-Datasource")
	Emitter<Datasource> _insertEmitter;

}
