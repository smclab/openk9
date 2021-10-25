package io.openk9.datasource.emitter.datasource;

import io.openk9.datasource.model.Datasource;
import io.quarkus.arc.Unremovable;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
@Unremovable
public class UpdateEmitter {

	public void send(Datasource datasource) {
		_updateEmitter.send(datasource);
	}

	@Inject
	@Channel("datasource-UPDATE-Datasource")
	Emitter<Datasource> _updateEmitter;

}
