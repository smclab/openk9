package io.openk9.tenantmanager.graphql;

import io.openk9.tenantmanager.model.BackgroundProcess;
import io.openk9.tenantmanager.service.BackgroundProcessService;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class BackgroundProcessGraphqlResource {

	@Query
	public Uni<List<BackgroundProcess>> getBackgroundProcess(UUID processId) {
		return backgroundProcessService
			.findBackgroundProcessListByProcessId(processId);
	}

	@Inject
	BackgroundProcessService backgroundProcessService;

}
