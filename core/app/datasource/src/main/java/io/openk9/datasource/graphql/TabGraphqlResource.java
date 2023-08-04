package io.openk9.datasource.graphql;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.Response;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.mapper.TokenTabMapper;
import io.openk9.datasource.model.SuggestionCategory;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.model.dto.TabDTO;
import io.openk9.datasource.service.TabService;
import io.openk9.datasource.service.TokenTabService;
import io.openk9.datasource.service.TranslationService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.openk9.datasource.service.util.Tuple2;
import io.openk9.datasource.sql.TransactionInvoker;
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
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;
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

	public Uni<Map<String, String>> getTranslationMap(@Source Tab tab) {
		return translationService.getTranslationMap(Tab.class, tab.getId());
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
	public Uni<Tuple2<Tab, TokenTab>> addTokenTabToTab(
		@Id long id, @Id long tokenTabId) {
		return _tabService.addTokenTabToTab(id, tokenTabId);
	}

	@Mutation
	public Uni<Tuple2<Tab, TokenTab>> removeTokenTabToTab(
		@Id long id, @Id long tokenTabId ) {
		return _tabService.removeTokenTabToTab(id, tokenTabId);
	}

	@Mutation
	public Uni<Tuple2<String, String>> addTabTranslation(
		@Id @Name("suggestionCategoryId") long suggestionCategoryId,
		String language, String key, String value) {

		return translationService
			.addTranslation(SuggestionCategory.class, suggestionCategoryId, language, key, value)
			.map((__) -> Tuple2.of("ok", null));
	}

	@Mutation
	public Uni<Tuple2<String, String>> deleteTabTranslation(
		@Id @Name("suggestionCategoryId") long suggestionCategoryId,
		String language, String key) {

		return translationService
			.deleteTranslation(SuggestionCategory.class, suggestionCategoryId, language, key)
			.map((__) -> Tuple2.of("ok", null));
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

	@Inject
	TranslationService translationService;

}
