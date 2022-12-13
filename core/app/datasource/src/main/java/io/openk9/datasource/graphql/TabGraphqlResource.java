package io.openk9.datasource.graphql;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.datasource.mapper.TokenTabMapper;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.model.dto.TabDTO;
import io.openk9.datasource.model.dto.TokenTabDTO;
import io.openk9.datasource.model.util.Mutiny2;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.service.TabService;
import io.openk9.datasource.service.TokenTabService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.openk9.datasource.service.util.Tuple2;
import io.openk9.datasource.sql.TransactionInvoker;
import io.openk9.datasource.validation.FieldValidator;
import io.openk9.datasource.validation.Response;
import io.openk9.datasource.web.SearchTokenDto;
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
import org.eclipse.microprofile.graphql.Source;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class TabGraphqlResource {

	@Query
	public Uni<Connection<Tab>> getTabs(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return _tabService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	public Uni<Connection<TokenTab>> tokenTabs(
		@Source Tab tab,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@Description("if notEqual is true, it returns unbound entities") @DefaultValue("false") boolean notEqual) {
		return getTokenTabs(
			tab.getId(), after, before, first, last, searchText, sortByList,
			notEqual);
	}

	public Uni<Connection<SearchTokenDto>> searchTokens(
		@Source Tab tab,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@Description("if notEqual is true, it returns unbound entities") @DefaultValue("false") boolean notEqual) {
		return getTokenTabs(
			tab.getId(), after, before, first, last, searchText, sortByList,
			notEqual).map(connection -> connection.map(_tokenTabMapper::toSearchTokenDto));
	}

	@Query
	public Uni<Connection<TokenTab>> getTokenTabs(
		@Id long tabId,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@Description("if notEqual is true, it returns unbound entities") @DefaultValue("false") boolean notEqual) {
		return _tabService.getTokenTabsConnection(
			tabId, after, before, first, last, searchText, sortByList, notEqual);
	}

	@Query
	public Uni<Tab> getTab(@Id long id) {
		return _tabService.findById(id);
	}

	@Query
	public Uni<TokenTab> getTokenTab(@Id long id) {
		return _tokenTabService.findById(id);
	}

	public Uni<Tab> tab(@Source TokenTab tokenTab) {
		return transactionInvoker.withTransaction(
			(s) -> Mutiny2.fetch(s, tokenTab.getTab()));
	}

	public Uni<Response<Tab>> patchTab(@Id long id, TabDTO tabDTO) {
		return _tabService.getValidator().patch(id, tabDTO);
	}

	public Uni<Response<Tab>> updateTab(@Id long id, TabDTO tabDTO) {
		return _tabService.getValidator().update(id, tabDTO);
	}

	public Uni<Response<Tab>> createTab(TabDTO tabDTO) {
		return _tabService.getValidator().create(tabDTO);
	}

	@Mutation
	public Uni<Response<Tab>> tab(
		@Id Long id, TabDTO tabDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createTab(tabDTO);
		} else {
			return patch
				? patchTab(id, tabDTO)
				: updateTab(id, tabDTO);
		}

	}

	@Mutation
	public Uni<Tab> deleteTab(@Id long tabId) {
		return _tabService.deleteById(tabId);
	}

	@Mutation
	public Uni<Response<TokenTab>> tokenTab(
		@Id long tabId, @Id Long tokenTabId, TokenTabDTO tokenTabDTO,
		@DefaultValue("false") boolean patch) {

		return Uni.createFrom().deferred(() -> {

			List<FieldValidator> validatorList =
				_tokenTabService.getValidator().validate(tokenTabDTO);

			if (validatorList.isEmpty()) {

				if (tokenTabId == null) {
					return _tabService.addTokenTab(tabId, tokenTabDTO)
						.map(e -> Response.of(e.right, null));
				} else {
					return (
						patch
							? _tokenTabService.patch(tokenTabId, tokenTabDTO)
							: _tokenTabService.update(tokenTabId, tokenTabDTO)
					).map(e -> Response.of(e, null));
				}

			}

			return Uni.createFrom().item(Response.of(null, validatorList));
		});

	}

	@Mutation
	public Uni<Tuple2<Tab, Long>> removeTokenTab(
		@Id long tabId, @Id Long tokenTabId) {
		return _tabService.removeTokenTab(tabId, tokenTabId);
	}

	@Subscription
	public Multi<Tab> tabCreated() {
		return _tabService
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<Tab> tabDeleted() {
		return _tabService
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<Tab> tabUpdated() {
		return _tabService
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	@Inject
	TabService _tabService;

	@Inject
	TokenTabService _tokenTabService;

	@Inject
	TransactionInvoker transactionInvoker;

	@Inject
	TokenTabMapper _tokenTabMapper;

}
