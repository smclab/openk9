package io.openk9.datasource.graphql;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.datasource.model.CharFilter;
import io.openk9.datasource.model.dto.CharFilterDTO;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.service.CharFilterService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.openk9.datasource.validation.Response;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class CharFilterGraphqlResource {

	@Query
	public Uni<Connection<CharFilter>> getCharFilters(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return _charFilterService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<CharFilter> getCharFilter(@Id long id) {
		return _charFilterService.findById(id);
	}

	public Uni<Response<CharFilter>> patchCharFilter(@Id long id, CharFilterDTO charFilterDTO) {
		return _charFilterService.getValidator().patch(id, charFilterDTO);
	}

	public Uni<Response<CharFilter>> updateCharFilter(@Id long id, CharFilterDTO charFilterDTO) {
		return _charFilterService.getValidator().update(id, charFilterDTO);
	}

	public Uni<Response<CharFilter>> createCharFilter(CharFilterDTO charFilterDTO) {
		return _charFilterService.getValidator().create(charFilterDTO);
	}

	@Mutation
	public Uni<Response<CharFilter>> charFilter(
		@Id Long id, CharFilterDTO charFilterDTO ,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createCharFilter(charFilterDTO);
		} else {
			return patch
				? patchCharFilter(id, charFilterDTO)
				: updateCharFilter(id, charFilterDTO);
		}

	}

	@Mutation
	public Uni<CharFilter> deleteCharFilter(@Id long charFilterId) {
		return _charFilterService.deleteById(charFilterId);
	}

	@Subscription
	public Multi<CharFilter> charFilterCreated() {
		return _charFilterService
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<CharFilter> charFilterDeleted() {
		return _charFilterService
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<CharFilter> charFilterUpdated() {
		return _charFilterService
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}


	@Inject
	CharFilterService _charFilterService;
}
