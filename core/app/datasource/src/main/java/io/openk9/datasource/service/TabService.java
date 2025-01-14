/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.datasource.service;

import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.graphql.dto.TabWithTokenTabsDTO;
import io.openk9.datasource.mapper.TabMapper;
import io.openk9.datasource.model.Sorting;
import io.openk9.datasource.model.Tab;
import io.openk9.datasource.model.Tab_;
import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.model.TokenTab_;
import io.openk9.datasource.model.dto.TabDTO;
import io.openk9.datasource.model.dto.TranslationDTO;
import io.openk9.datasource.model.dto.TranslationKeyDTO;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin;
import org.hibernate.FlushMode;
import org.hibernate.reactive.mutiny.Mutiny;

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
						var tokenTabIds = tabWithTokenTabsDTO.getTokenTabIds();

						UniJoin.Builder<Void> builder = Uni.join().builder();
						builder.add(Uni.createFrom().voidItem());

						if (tokenTabIds != null) {
							for (long tokenTabId : tokenTabIds) {
								builder.add(addTokenTabToTab(
									tab.getId(), tokenTabId)
									.replaceWithVoid()
								);
							}
						}

						return builder.joinAll()
							.usingConcurrencyOf(1)
							.andCollectFailures()
							.map(voids -> tab);
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

						UniJoin.Builder<Void> builder = Uni.join().builder();
						if (tokenTabIds != null) {
							var oldTokenTabs = newStateTab.getTokenTabs();

							for (TokenTab oldTokenTab : oldTokenTabs) {
								builder.add(removeTokenTabToTab(
									tabId, oldTokenTab.getId())
									.replaceWithVoid()
								);
							}

							for (long tokenTabId : tokenTabIds) {
								builder.add(addTokenTabToTab(tabId, tokenTabId)
									.replaceWithVoid()
								);
							}
						}

						return builder.joinAll()
							.usingConcurrencyOf(1)
							.andCollectFailures()
							.map(voids -> tab);
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
						var oldTokenTabs = tab.getTokenTabs();

						var tokenTabIds = tabWithTokenTabsDTO.getTokenTabIds();

						UniJoin.Builder<Void> builder = Uni.join().builder();

						for (TokenTab oldTokenTab : oldTokenTabs) {
							builder.add(removeTokenTabToTab(
								tabId, oldTokenTab.getId())
								.replaceWithVoid()
							);
						}

						if (tokenTabIds != null) {
							for (long tokenTabId : tokenTabIds) {
								builder.add(addTokenTabToTab(tabId, tokenTabId)
									.replaceWithVoid()
								);
							}
						}

						return builder.joinAll()
							.usingConcurrencyOf(1)
							.andCollectFailures()
							.map(voids -> tab);
					}));
		}

		return super.update(tabId, tabDTO);
	}

	public Uni<List<Tab>> findUnboundTabsByTokenTab(long tokenTabId) {

		return sessionFactory.withTransaction(s -> {
			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			CriteriaQuery<Tab> criteriaQuery = cb.createQuery(Tab.class);
			Root<Tab> rootTab = criteriaQuery.from(Tab.class);

			criteriaQuery.select(rootTab);

			Subquery<Long> idsToExcludeQuery = criteriaQuery.subquery(Long.class);
			Root<Tab> rootTabToExclude = idsToExcludeQuery.from(Tab.class);

			Join<Tab, TokenTab> tokenTabJoinToExclude =
					rootTabToExclude.join(Tab_.tokenTabs, JoinType.INNER);

			idsToExcludeQuery
					.select(rootTabToExclude.get(Tab_.id))
					.where(cb.equal(tokenTabJoinToExclude.get(TokenTab_.id), tokenTabId));

			criteriaQuery.where(
					cb.not(rootTab.get(Tab_.id).in(idsToExcludeQuery)));

			return s.createQuery(criteriaQuery).getResultList();
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
