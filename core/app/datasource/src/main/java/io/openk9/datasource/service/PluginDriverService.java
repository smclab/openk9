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

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.mapper.PluginDriverMapper;
import io.openk9.datasource.model.AclMapping;
import io.openk9.datasource.model.AclMapping_;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.PluginDriverDocTypeFieldKey;
import io.openk9.datasource.model.PluginDriverDocTypeFieldKey_;
import io.openk9.datasource.model.PluginDriver_;
import io.openk9.datasource.model.dto.PluginDriverDTO;
import io.openk9.datasource.model.util.K9Entity_;
import io.openk9.datasource.model.util.Mutiny2;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.graphql.Query;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class PluginDriverService
	extends BaseK9EntityService<PluginDriver, PluginDriverDTO> {
	PluginDriverService(PluginDriverMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public Class<PluginDriver> getEntityClass() {
		return PluginDriver.class;
	}

	@Override
	public String[] getSearchFields() {
		return new String[]{
			PluginDriver_.NAME, PluginDriver_.DESCRIPTION, PluginDriver_.TYPE};
	}

	@Query
	public Uni<Connection<DocTypeField>> getDocTypeFieldsConnection(
		long pluginDriverId, String after,
		String before, Integer first, Integer last, String searchText,
		Set<SortBy> sortByList, boolean notEqual) {

		return findJoinConnection(
			pluginDriverId, PluginDriver_.ACL_MAPPINGS, DocTypeField.class,
			docTypeFieldService.getSearchFields(), after, before, first,
			last, searchText, sortByList, notEqual, pluginDriverRoot ->
				pluginDriverRoot
					.join(PluginDriver_.aclMappings)
					.join(AclMapping_.docTypeField),
			(cb, pluginDriverRoot) ->
				pluginDriverRoot
					.getJoins()
					.stream()
					.filter(e -> Objects.equals(e.getAttribute(),
						PluginDriver_.aclMappings))
					.map(e -> (Join<PluginDriver, AclMapping>) e)
					.map(e -> e.get(AclMapping_.userField))
					.map(cb::asc)
					.collect(Collectors.toList()));

	}

	public Uni<Page<DocTypeField>> getDocTypeFields(
		long pluginDriverId, Pageable pageable) {
		return getDocTypeFields(pluginDriverId, pageable, Filter.DEFAULT);
	}

	public Uni<Page<DocTypeField>> getDocTypeFields(
		long pluginDriverId, Pageable pageable, String searchText) {

		return findAllPaginatedJoin(
			new Long[]{pluginDriverId},
			PluginDriver_.ACL_MAPPINGS, DocTypeField.class,
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(),
			searchText);
	}

	public Uni<Set<DocTypeField>> getDocTypeFieldsInPluginDriver(
		long pluginDriverId) {

		return withTransaction(
			s ->
				findById(pluginDriverId)
					.flatMap(
						ep -> Mutiny2.fetch(s, ep.getAclMappings()))
					.map(l -> l
						.stream()
						.map(AclMapping::getDocTypeField)
						.collect(Collectors.toSet()))
		);

	}

	public Uni<List<DocTypeField>> getDocTypeFieldsNotInPluginDriver(
		long pluginDriverId) {

		return withTransaction(
			s -> {

				CriteriaBuilder cb = em.getCriteriaBuilder();

				CriteriaQuery<DocTypeField> query =
					cb.createQuery(DocTypeField.class);

				Root<DocTypeField> root = query.from(DocTypeField.class);

				Subquery<Long> subquery = query.subquery(Long.class);

				Root<PluginDriver> from = subquery.from(PluginDriver.class);

				SetJoin<PluginDriver, AclMapping> rootJoin =
					from.joinSet(PluginDriver_.ACL_MAPPINGS);

				Path<PluginDriverDocTypeFieldKey> pluginDriverDocTypeFieldKeyPath =
					rootJoin.get(AclMapping_.key);

				Path<Long> docTypeFieldId = pluginDriverDocTypeFieldKeyPath.get(
					PluginDriverDocTypeFieldKey_.docTypeFieldId);

				subquery.select(docTypeFieldId);

				subquery.where(
					cb.equal(from.get(K9Entity_.id), pluginDriverId));

				query.where(
					cb.in(root.get(K9Entity_.id)).value(subquery).not());

				return s.createQuery(query).getResultList();

			}
		);
	}

	public Uni<Page<DocTypeField>> getDocTypeFields(
		long pluginDriverId, Pageable pageable,
		Filter filter) {

		return findAllPaginatedJoin(
			new Long[] { pluginDriverId },
			PluginDriver_.ACL_MAPPINGS, DocTypeField.class,
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(),
			filter);
	}

	public Uni<Tuple2<PluginDriver, DocTypeField>> addDocTypeField(
		long pluginDriverId, long docTypeFieldId, AclMapping.UserField userField) {

		return withTransaction((s) -> findById(pluginDriverId)
			.onItem()
			.ifNotNull()
			.transformToUni(pluginDriver ->
				docTypeFieldService.findById(docTypeFieldId)
					.onItem()
					.ifNotNull()
					.transformToUni(docTypeField ->
						Mutiny2
							.fetch(s, pluginDriver.getAclMappings())
							.flatMap(aclMappings -> {

								AclMapping newAclMapping =
									AclMapping.of(
										PluginDriverDocTypeFieldKey.of(
											pluginDriverId, docTypeFieldId),
										pluginDriver, docTypeField, userField
									);

								if (aclMappings.add(newAclMapping)) {
									pluginDriver.setAclMappings(aclMappings);
									return persist(pluginDriver).map(ep -> Tuple2.of(ep, docTypeField));
								} else {
									return Uni.createFrom().nullItem();
								}

							})
					)
			)
		);
	}

	public Uni<Tuple2<PluginDriver, DocTypeField>> removeDocTypeField(long pluginDriverId, long docTypeFieldId) {
		return withTransaction((s) -> findById(pluginDriverId)
			.onItem()
			.ifNotNull()
			.transformToUni(pluginDriver ->
				docTypeFieldService.findById(docTypeFieldId)
					.onItem()
					.ifNotNull()
					.transformToUni(docTypeField ->
						Mutiny2
							.fetch(s, pluginDriver.getAclMappings())
							.flatMap(aclMappings -> {

								boolean removed = aclMappings.removeIf(
									epi -> epi.getKey().getDocTypeFieldId() == docTypeFieldId
										   && epi.getKey().getPluginDriverId() == pluginDriverId);

								if (removed) {
									return s.find(
											AclMapping.class,
											PluginDriverDocTypeFieldKey.of(pluginDriverId,docTypeFieldId)
										)
										.call(s::remove)
										.map(ep -> Tuple2.of(pluginDriver, docTypeField));
								} else {
									return Uni.createFrom().nullItem();
								}

							}))));
	}

	@Inject
	DocTypeFieldService docTypeFieldService;
}
