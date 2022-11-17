package io.openk9.datasource.graphql;

import io.openk9.datasource.graphql.util.relay.Connection;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.resource.util.SortBy;
import io.openk9.datasource.service.TokenTabService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class TokenTabGraphqlResource {

	public Uni<DocTypeField> docTypeField(@Source TokenTab tokenTab) {
		return tokenTabService.getDocTypeField(tokenTab.getId());
	}

	@Mutation
	public Uni<Tuple2<TokenTab, DocTypeField>> bindDocTypeFieldToTokenTab(
		@Id long tokenTabId, @Id long docTypeFieldId) {
		return tokenTabService.bindDocTypeFieldToTokenTab(tokenTabId, docTypeFieldId);
	}

	@Mutation
	public Uni<Tuple2<TokenTab, DocTypeField>> unbindDocTypeFieldFromTokenTab(
		@Id long id, @Id long docTypeFieldId) {
		return tokenTabService.unbindDocTypeFieldFromTokenTab(id, docTypeFieldId);
	}

	public Uni<Connection<DocTypeField>> docTypeFieldsNotInTokenTab(
		@Source TokenTab tokenTab,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {

		return tokenTabService.getDocTypeFieldsNotInTokenTab(
			tokenTab.getId(), after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<Connection<DocTypeField>> getDocTypeFieldsNotInTokenTab(
		@Id long tokenTabId,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@Description("if notEqual is true, it returns unbound entities") @DefaultValue("false") boolean notEqual) {
		return tokenTabService.getDocTypeFieldsNotInTokenTab(
			tokenTabId, after, before, first, last, searchText, sortByList);
	}

	@Inject
	TokenTabService tokenTabService;
}
