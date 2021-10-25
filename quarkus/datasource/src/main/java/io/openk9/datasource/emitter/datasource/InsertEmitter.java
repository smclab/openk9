package io.openk9.datasource.emitter.datasource;

import io.openk9.datasource.model.Datasource;
import io.quarkus.arc.Unremovable;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@Unremovable
public class InsertEmitter {

	public void send(Datasource datasource) {
		_insertEmitter.send(datasource);
	}

	@Inject
	@Channel("datasource-INSERT-Datasource")
	Emitter<Datasource> _insertEmitter;

}
