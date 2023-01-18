package io.openk9.tenantmanager.graphql;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.model.EntityServiceValidatorWrapper;
import io.openk9.common.util.Response;
import io.openk9.common.util.SortBy;
import io.openk9.tenantmanager.dto.TenantDTO;
import io.openk9.tenantmanager.mapper.TenantMapper;
import io.openk9.tenantmanager.model.Tenant;
import io.openk9.tenantmanager.service.TenantService;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
@RolesAllowed("admin")
public class TenantGraphqlResource {

	@Query
	public Uni<Connection<Tenant>> getTenants(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return tenantService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<Tenant> getTenant(@Id long id) {
		return tenantService.findById(id);
	}

	@Mutation
	public Uni<Response<Tenant>> tenant(
		@Id Long id, TenantDTO tenantDTO,
		@DefaultValue("false") boolean patch) {

		EntityServiceValidatorWrapper<Tenant, TenantDTO> validator =
			tenantService.getValidator();

		if (id != null) {
			if (patch) {
				return validator.patch(id, tenantDTO);
			}
			else {
				return validator.update(id, tenantDTO);
			}
		}

		return validator.create(tenantDTO);

	}

	@Inject
	TenantService tenantService;

	@Inject
	TenantMapper tenantMapper;

}
