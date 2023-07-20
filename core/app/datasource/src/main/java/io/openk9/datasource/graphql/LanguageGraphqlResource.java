package io.openk9.datasource.graphql;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.Response;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.model.Language;
import io.openk9.datasource.model.dto.LanguageDTO;
import io.openk9.datasource.service.LanguageService;
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
public class LanguageGraphqlResource {

	@Query
	public Uni<Connection<Language>> getLanguages(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return languageService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<Language> getLanguage(@Id long id) {
		return languageService.findById(id);
	}

	public Uni<Response<Language>> patchLanguage(@Id long id, LanguageDTO languageDTO) {
		return languageService.getValidator().patch(id, languageDTO);
	}

	public Uni<Response<Language>> updateLanguage(@Id long id, LanguageDTO languageDTO) {
		return languageService.getValidator().update(id, languageDTO);
	}

	public Uni<Response<Language>> createLanguage(LanguageDTO languageDTO) {
		return languageService.getValidator().create(languageDTO);
	}

	@Mutation
	public Uni<Response<Language>> language(
		@Id Long id, LanguageDTO languageDTO, @DefaultValue("false") boolean patch) {

		if (id == null) {
			return createLanguage(languageDTO);
		} else {
			return patch
				? patchLanguage(id, languageDTO)
				: updateLanguage(id, languageDTO);
		}

	}

	@Mutation
	public Uni<Language> deleteLanguage(@Id long languageId) {
		return languageService.deleteById(languageId);
	}

	@Inject
	LanguageService languageService;

}


