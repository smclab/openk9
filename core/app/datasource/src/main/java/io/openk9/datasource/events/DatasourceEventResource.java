package io.openk9.datasource.events;

import io.smallrye.mutiny.Multi;
import org.eclipse.microprofile.reactive.messaging.Channel;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/datasource-event")
@RolesAllowed("k9-admin")
public class DatasourceEventResource {

	@GET
	@Produces(MediaType.SERVER_SENT_EVENTS)
	public Multi<DatasourceEvent> datasourceEvents() {
		return datasourceEvents;
	}

	@GET
	@Produces(MediaType.SERVER_SENT_EVENTS)
	@Path("/{datasourceId}")
	public Multi<DatasourceEvent> datasourceEvents(@PathParam("datasourceId") long datasourceId) {
		return datasourceEvents
			.select()
			.where(e -> e.datasourceId() == datasourceId);
	}

	@Channel("datasource-events")
	Multi<DatasourceEvent> datasourceEvents;


}