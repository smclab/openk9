package io.openk9.datasource.events;

import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.reactive.messaging.Channel;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/datasource-event")
@RolesAllowed("k9-admin")
public class DatasourceEventResource {

	@GET
	@Produces(MediaType.SERVER_SENT_EVENTS)
	public Multi<DatasourceMessage> datasourceEvents() {
		return datasourceEvents;
	}

	@Inject
	@Channel("datasource-events")
	Multi<DatasourceMessage> datasourceEvents;


}