package io.openk9.datasource.graphql;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.Response;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.model.dto.TokenTabDTO;
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

	@Query
	public Uni<TokenTab> getTokenTab(@Id long id) {
		return tokenTabService.findById(id);
	}

	@Query
	public Uni<Connection<TokenTab>> getTotalTokenTabs(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return tokenTabService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	public Uni<DocTypeField> docTypeField(@Source TokenTab tokenTab) {
		return tokenTabService.getDocTypeField(tokenTab);
	}

	public Uni<Set<TokenTab.ExtraParam>> extraParams(@Source TokenTab tokenTab) {
		return tokenTabService.getExtraParams(tokenTab);
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

	@Mutation
	public Uni<TokenTab> deleteTokenTab(@Id long tokenTabId) {
		return tokenTabService.deleteById(tokenTabId);
	}

	@Mutation
	public Uni<Response<TokenTab>> tokenTab(
		@Id Long id, TokenTabDTO tokenTabDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createTokenTab(tokenTabDTO);
		} else {
			return patch
				? tokenTab(id, tokenTabDTO)
				: updateTokenTab(id, tokenTabDTO);
		}

	}

	@Mutation
	public Uni<TokenTab> addExtraParam(@Id long id, String key, String value) {
		return tokenTabService.addExtraParam(id, key, value);
	}

	@Mutation
	public Uni<TokenTab> removeExtraParam(@Id int id, String key) {
		return tokenTabService.removeExtraParam(id, key);
	}

	public Uni<Response<TokenTab>> createTokenTab(TokenTabDTO tokenTabDTO) {
		return tokenTabService.getValidator().create(tokenTabDTO);
	}

	public Uni<Response<TokenTab>> tokenTab(@Id long id, TokenTabDTO tokenTabDTO) {
		return tokenTabService.getValidator().patch(id, tokenTabDTO);
	}

	public Uni<Response<TokenTab>> updateTokenTab(@Id long id, TokenTabDTO tokenTabDTO) {
		return tokenTabService.getValidator().update(id, tokenTabDTO);
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
