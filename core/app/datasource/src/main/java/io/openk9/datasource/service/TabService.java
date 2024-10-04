package io.openk9.datasource.service;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.graphql.dto.TabWithTokenTabsDTO;
import io.openk9.datasource.mapper.TabMapper;
import io.openk9.datasource.model.Analyzer;
import io.openk9.datasource.model.Sorting;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.Tab_;
import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.model.dto.TabDTO;
import io.openk9.datasource.model.dto.TranslationDTO;
import io.openk9.datasource.model.dto.TranslationKeyDTO;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;
import org.hibernate.FlushMode;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class TabService extends BaseK9EntityService<Tab, TabDTO> {
	TabService(TabMapper mapper) {
		this.mapper = mapper;
	}

	public Uni<Tab> create(TabDTO tabDTO){
		if (tabDTO instanceof TabWithTokenTabsDTO tabWithTokenTabsDTO) {

			var transientTab = mapper.create(tabWithTokenTabsDTO);

			return sessionFactory.withTransaction(
				(s, transaction) -> super.create(s, transientTab)
					.flatMap(tab -> {
						var tokenTabs =
							tabWithTokenTabsDTO.getTokenTabIds().stream()
								.map(tokenTabId -> s.getReference(TokenTab.class, tokenTabId))
								.collect(Collectors.toSet());

						tab.setTokenTabs(tokenTabs);

						return s.persist(tab)
							.flatMap(__ -> s.merge(tab));
					})
			);
		}

		return super.create(tabDTO);
	}

	public Uni<Tab> patch(long tabId, TabDTO tabDTO) {
		if (tabDTO instanceof TabWithTokenTabsDTO tabWithTokenTabsDTO) {

			return sessionFactory.withTransaction(
				(s, transaction) -> findById(s, tabId)
					.call(tab -> Mutiny.fetch(tab.getTokenTabs()))
					.flatMap(tab -> {
						var newStateTab = mapper.patch(tab, tabWithTokenTabsDTO);
						var tokenTabIds = tabWithTokenTabsDTO.getTokenTabIds();

						if (tokenTabIds != null) {
							var tokenTabs = tokenTabIds.stream()
								.map(tokenTabId -> s.getReference(TokenTab.class, tokenTabId))
								.collect(Collectors.toSet());

							newStateTab.getTokenTabs().clear();
							newStateTab.setTokenTabs(tokenTabs);
						}

						return s.merge(newStateTab)
							.map(__ -> newStateTab);
					}));
		}

		return super.patch(tabId, tabDTO);
	}

	public Uni<Tab> update(long tabId, TabDTO tabDTO) {
		if (tabDTO instanceof TabWithTokenTabsDTO tabWithTokenTabsDTO) {

			return sessionFactory.withTransaction(
				(s, transaction) -> findById(s, tabId)
					.call(tab -> Mutiny.fetch(tab.getTokenTabs()))
					.flatMap(tab -> {
						var newStateTab = mapper.update(tab, tabWithTokenTabsDTO);
						var tokenTabIds = tabWithTokenTabsDTO.getTokenTabIds();

						newStateTab.getTokenTabs().clear();

						if (tokenTabIds != null) {
							var tokenTabs = tokenTabIds.stream()
								.map(tokenTabId -> s.getReference(TokenTab.class, tokenTabId))
								.collect(Collectors.toSet());

							newStateTab.setTokenTabs(tokenTabs);
						}

						return s.merge(newStateTab)
							.map(__ -> newStateTab);
					}));
		}

		return super.update(tabId, tabDTO);
	}

	public Uni<List<Tab>> findUnboundTabsByTokenTab(long tokenTabId) {
		return sessionFactory.withTransaction(s -> {
			String queryString = "SELECT tab.* " +
				"WHERE tab.id not in (" +
				"SELECT tab_token_tab.tab_id FROM tab_token_tab " +
				"WHERE tab_token_tab.token_tab_id = (:tokenTabId))";

			return s.createNativeQuery(queryString, Tab.class)
				.setParameter("tokenTabId", tokenTabId)
				.getResultList();
		});
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {Tab_.NAME, Tab_.DESCRIPTION};
	}

	public Uni<Connection<TokenTab>> getTokenTabsConnection(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {
		return findJoinConnection(
			id, Tab_.TOKEN_TABS, TokenTab.class,
			_tokenTabService.getSearchFields(), after, before, first, last,
			searchText, sortByList, notEqual);
	}


	public Uni<Page<TokenTab>> getTokenTabs(
		long tabId, Pageable pageable) {
		return getTokenTabs(tabId, pageable, Filter.DEFAULT);
	}

	public Uni<Page<TokenTab>> getTokenTabs(
		long tabId, Pageable pageable, String searchText) {

		return findAllPaginatedJoin(
			new Long[]{tabId},
			Tab_.TOKEN_TABS, TokenTab.class,
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(),
			searchText);
	}

	public Uni<Page<TokenTab>> getTokenTabs(
		long tabId, Pageable pageable, Filter filter) {

		return findAllPaginatedJoin(
			new Long[]{tabId},
			Tab_.TOKEN_TABS, TokenTab.class,
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(),
			filter);
	}

	public Uni<Set<TokenTab>> getTokenTabs(Tab tab) {
		return sessionFactory.withTransaction(s -> s.fetch(tab.getTokenTabs()));
	}

	public Uni<List<TokenTab>> getTokenTabsByName(String tabName) {
		return sessionFactory.withTransaction((s) -> {
			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
			CriteriaQuery<TokenTab> cq = cb.createQuery(TokenTab.class);
			Root<Tab> root = cq.from(Tab.class);
			cq.select(root.join(Tab_.TOKEN_TABS));
			cq.where(cb.equal(root.get(Tab_.name), tabName));
			return s.createQuery(cq).getResultList();
		});
	}

	public Uni<Tuple2<Tab, TokenTab>> addTokenTabToTab(long tabId, long tokenTabId) {

		return sessionFactory.withTransaction((s, tr) -> findById(s, tabId)
			.onItem()
			.ifNotNull()
			.transformToUni(tab -> _tokenTabService.findById(s, tokenTabId)
				.onItem()
				.ifNotNull()
				.transformToUni(tokenTab -> s.
					fetch(tab.getTokenTabs())
					.flatMap(tokenTabs -> {
						if (tokenTabs.add(tokenTab)) {
							tab.setTokenTabs(tokenTabs);
							return persist(s, tab).map(newD -> Tuple2.of(newD, tokenTab));
						}
						return Uni.createFrom().nullItem();
					})
				)
			)
		);

	}

	public Uni<Tuple2<Tab, TokenTab>> removeTokenTabToTab(long tabId, long tokenTabId) {

		return sessionFactory.withTransaction((s, tr) -> findById(s, tabId)
			.onItem()
			.ifNotNull()
			.transformToUni(tab -> _tokenTabService.findById(s, tokenTabId)
				.onItem()
				.ifNotNull()
				.transformToUni(tokenTab -> s
					.fetch(tab.getTokenTabs())
					.flatMap(tokenTabs -> {
						if (tab.removeTokenTab(tokenTabs, tokenTabId)) {
							return persist(s, tab).map(newD -> Tuple2.of(newD, tokenTab));
						}
						return Uni.createFrom().nullItem();
					})
				)
			)
		);

	}

	public Uni<Connection<Sorting>> getSortingsConnection(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {
		return findJoinConnection(
			id, Tab_.SORTINGS, Sorting.class,
			_sortingService.getSearchFields(), after, before, first, last,
			searchText, sortByList, notEqual);
	}


	public Uni<Page<Sorting>> getSortings(
		long sortingId, Pageable pageable) {
		return getSortings(sortingId, pageable, Filter.DEFAULT);
	}

	public Uni<Page<Sorting>> getSortings(
		long sortingId, Pageable pageable, String searchText) {

		return findAllPaginatedJoin(
			new Long[]{sortingId},
			Tab_.SORTINGS, Sorting.class,
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(),
			searchText);
	}

	public Uni<Page<Sorting>> getSortings(
		long sortingId, Pageable pageable, Filter filter) {

		return findAllPaginatedJoin(
			new Long[]{sortingId},
			Tab_.SORTINGS, Sorting.class,
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(),
			filter);
	}

	public Uni<Set<Sorting>> getSortings(Tab tab) {
		return sessionFactory.withTransaction(s -> s.fetch(tab.getSortings()));
	}

	public Uni<Tuple2<Tab, Sorting>> addSortingToTab(long tabId, long sortingId) {

		return sessionFactory.withTransaction((s, tr) -> findById(s, tabId)
			.onItem()
			.ifNotNull()
			.transformToUni(tab -> _sortingService.findById(s, sortingId)
				.onItem()
				.ifNotNull()
				.transformToUni(sorting -> s.
					fetch(tab.getSortings())
					.flatMap(sortings -> {
						if (sortings.add(sorting)) {
							tab.setSortings(sortings);
							return persist(s, tab).map(newD -> Tuple2.of(newD, sorting));
						}
						return Uni.createFrom().nullItem();
					})
				)
			)
		);

	}

	public Uni<Tuple2<Tab, Sorting>> removeSortingToTab(long tabId, long sortingId) {

		return sessionFactory.withTransaction((s, tr) -> findById(s, tabId)
			.onItem()
			.ifNotNull()
			.transformToUni(tab -> _sortingService.findById(s, sortingId)
				.onItem()
				.ifNotNull()
				.transformToUni(sorting -> s
					.fetch(tab.getSortings())
					.flatMap(sortings -> {
						if (tab.removeSorting(sortings, sortingId)) {
							return persist(s, tab).map(newD -> Tuple2.of(newD, sorting));
						}
						return Uni.createFrom().nullItem();
					})
				)
			)
		);

	}

	public Uni<Tab> findByName(String name) {
		return sessionFactory.withTransaction((s) -> {
			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
			CriteriaQuery<Tab> cq = cb.createQuery(Tab.class);
			Root<Tab> root = cq.from(Tab.class);
			cq.where(cb.equal(root.get(Tab_.name), name));
			return s.createQuery(cq)
				.setFlushMode(FlushMode.MANUAL)
				.getSingleResultOrNull();
		});
	}

	public Uni<List<Tab>> getTabListByNames(String[] tabNames) {
		return sessionFactory.withTransaction(s -> {

			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			Class<Tab> entityClass = getEntityClass();

			CriteriaQuery<Tab> query = cb.createQuery(entityClass);

			Root<Tab> from = query.from(entityClass);

			query.where(from.get(Tab_.name).in(List.of(tabNames)));

			return s
				.createQuery(query)
				.getResultList();

		});
	}

	public Uni<Boolean> existsByName(String name) {
		return sessionFactory.withTransaction(s -> {

			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			Class<Tab> entityClass = getEntityClass();

			CriteriaQuery<Long> query = cb.createQuery(Long.class);

			Root<Tab> from = query.from(entityClass);

			query.select(cb.count(from));

			query.where(cb.equal(from.get(Tab_.name), name));

			return s
				.createQuery(query)
				.getSingleResult()
				.map(count -> count > 0);

		});

	}

	public Uni<Void> addTranslation(Long id, TranslationDTO dto) {
		return translationService.addTranslation(
			Tab.class, id, dto.getLanguage(), dto.getKey(), dto.getValue());
	}

	public Uni<Void> deleteTranslation(Long id, TranslationKeyDTO dto) {
		return translationService.deleteTranslation(
			Tab.class, id, dto.getLanguage(), dto.getKey());
	}


	@Inject
	TokenTabService _tokenTabService;

	@Inject
	SortingService _sortingService;

	@Inject
	TranslationService translationService;

	@Override
	public Class<Tab> getEntityClass() {
		return Tab.class;
	}

}
