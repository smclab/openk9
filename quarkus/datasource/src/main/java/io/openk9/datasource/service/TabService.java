package io.openk9.datasource.service;

import io.openk9.datasource.graphql.util.relay.Connection;
import io.openk9.datasource.mapper.TabMapper;
import io.openk9.datasource.mapper.TokenTabMapper;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.Tab_;
import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.model.dto.TabDTO;
import io.openk9.datasource.model.dto.TokenTabDTO;
import io.openk9.datasource.model.util.Mutiny2;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.resource.util.SortBy;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;
import org.hibernate.FlushMode;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Set;

@RequestScoped
public class TabService extends BaseK9EntityService<Tab, TabDTO> {
	TabService(TabMapper mapper) {
		this.mapper = mapper;
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

	public Uni<List<TokenTab>> getTokenTabs(Tab tab) {
		return withTransaction(s -> Mutiny2.fetch(s, tab.getTokenTabs()));
	}

	public Uni<Tuple2<Tab, TokenTab>> addTokenTab(
		long id, TokenTabDTO tokenTabDTO) {

		TokenTab tokenTab =
			_tokenTabMapper.create(tokenTabDTO);

		return withTransaction((s) -> findById(id)
			.onItem()
			.ifNotNull()
			.transformToUni(tab -> Mutiny2.fetch(s, tab.getTokenTabs()).flatMap(
				tokenTabs -> {
					if (tab.addTokenTab(tokenTabs, tokenTab)) {
						return persist(tab)
							.map(dt -> Tuple2.of(dt, tokenTab));
					}
					return Uni.createFrom().nullItem();
				})));
	}

	public Uni<Tuple2<Tab, Long>> removeTokenTab(long id, long tokenTabId) {
		return withTransaction((s) -> findById(id)
			.onItem()
			.ifNotNull()
			.transformToUni(tab -> Mutiny2.fetch(s, tab.getTokenTabs()).flatMap(
				tokenTabs -> {
					if (tab.removeTokenTab(tokenTabs, tokenTabId)) {
						return persist(tab)
							.map(dt -> Tuple2.of(dt, tokenTabId));
					}
					return Uni.createFrom().nullItem();
				})));
	}

	public Uni<List<TokenTab>> getTokenTabsByName(String tabName) {
		return withStatelessTransaction((s) -> {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<TokenTab> cq = cb.createQuery(TokenTab.class);
			Root<Tab> root = cq.from(Tab.class);
			cq.select(root.join(Tab_.TOKEN_TABS));
			cq.where(cb.equal(root.get(Tab_.name), tabName));
			return s.createQuery(cq).getResultList();
		});
	}

	public Uni<Tab> findByName(String name) {
		return withStatelessTransaction((s) -> {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaQuery<Tab> cq = cb.createQuery(Tab.class);
			Root<Tab> root = cq.from(Tab.class);
			cq.where(cb.equal(root.get(Tab_.name), name));
			return s.createQuery(cq)
				.setFlushMode(FlushMode.MANUAL)
				.getSingleResultOrNull();
		});
	}

	public Uni<List<Tab>> getTabListByNames(String[] tabNames) {
		return withTransaction(s -> {

			CriteriaBuilder cb = em.getCriteriaBuilder();

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
		return withTransaction(s -> {

			CriteriaBuilder cb = em.getCriteriaBuilder();

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



	@Inject
	TokenTabService _tokenTabService;

	@Inject
	TokenTabMapper _tokenTabMapper;

	@Override
	public Class<Tab> getEntityClass() {
		return Tab.class;
	}

}
