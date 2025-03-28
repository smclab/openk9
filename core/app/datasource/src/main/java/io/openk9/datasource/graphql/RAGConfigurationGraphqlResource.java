package io.openk9.datasource.graphql;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.Response;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.model.RAGConfiguration;
import io.openk9.datasource.model.dto.RAGConfigurationDTO;
import io.openk9.datasource.service.RAGConfigurationService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class RAGConfigurationGraphqlResource {

	@Inject
	RAGConfigurationService service;

	@Mutation
	public Uni<RAGConfiguration> deleteRAGConfiguration(@Id long id) {
		return service.deleteById(id);
	}

	@Query
	public Uni<RAGConfiguration> getRAGConfiguration(@Id long id) {
		return service.findById(id);
	}

	@Query
	public Uni<Connection<RAGConfiguration>> getRAGConfigurations(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {

		return service.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	@Mutation
	public Uni<Response<RAGConfiguration>> ragConfiguration(
		@Id Long id,
		RAGConfigurationDTO ragConfigurationDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createRAGConfiguration(ragConfigurationDTO);
		}
		else {
			return patch
				? patchRAGConfiguration(id, ragConfigurationDTO)
				: updateRAGConfiguration(id, ragConfigurationDTO);
		}
	}

	protected Uni<Response<RAGConfiguration>> createRAGConfiguration(
		RAGConfigurationDTO ragConfigurationDTO) {

		return service.getValidator().create(ragConfigurationDTO);
	}

	protected Uni<Response<RAGConfiguration>> patchRAGConfiguration(
		@Id long id,
		RAGConfigurationDTO ragConfigurationDTO) {

		return service.getValidator().patch(id, ragConfigurationDTO);
	}

	protected Uni<Response<RAGConfiguration>> updateRAGConfiguration(
		@Id long id,
		RAGConfigurationDTO ragConfigurationDTO) {

		return service.getValidator().update(id, ragConfigurationDTO);
	}
}
