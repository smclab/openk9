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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import io.openk9.datasource.mapper.ResultAclConfigMapper;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.ResultAclConfig;
import io.openk9.datasource.model.ResultAclConfig_;
import io.openk9.datasource.model.dto.base.ResultAclConfigDTO;
import io.openk9.datasource.service.exception.InvalidDocTypeFieldSetException;

import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

/**
 * Manages the tenant {@link ResultAclConfig}, the single configuration of the
 * search mode that applies the ACLs to the results.
 */
@ApplicationScoped
public class ResultAclConfigService
	extends BaseK9EntityService<ResultAclConfig, ResultAclConfigDTO> {

	@Inject
	DocTypeFieldService docTypeFieldService;

	ResultAclConfigService(ResultAclConfigMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public Class<ResultAclConfig> getEntityClass() {
		return ResultAclConfig.class;
	}

	/**
	 * Returns the configuration of the current tenant.
	 *
	 * @return the configuration, or {@code null} when the tenant has none
	 */
	public Uni<ResultAclConfig> findCurrent() {
		return sessionFactory.withTransaction(
			(session, transaction) -> findCurrent(session)
		);
	}

	/**
	 * Returns the configuration of the tenant bound to the given session.
	 *
	 * @param session the session of the tenant
	 * @return the configuration, or {@code null} when the tenant has none
	 */
	public Uni<ResultAclConfig> findCurrent(Mutiny.Session session) {
		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

		CriteriaQuery<ResultAclConfig> query =
			cb.createQuery(ResultAclConfig.class);
		Root<ResultAclConfig> root = query.from(ResultAclConfig.class);

		query.where(cb.equal(
			root.get(ResultAclConfig_.name), ResultAclConfig.DEFAULT_NAME));

		return session.createQuery(query).getSingleResultOrNull();
	}

	@Override
	public Uni<ResultAclConfig> create(ResultAclConfigDTO dto) {
		return sessionFactory.withTransaction(
			(session, transaction) -> {
				var transientConfig = mapper.create(dto);
				transientConfig.setEnabled(Boolean.TRUE.equals(dto.getEnabled()));

				return findVisibleFields(session, dto.getVisibleFieldIds())
					.flatMap(visibleFields -> {
						transientConfig.setVisibleFields(visibleFields);

						return create(session, transientConfig);
					});
			}
		);
	}

	@Override
	public Uni<ResultAclConfig> update(long id, ResultAclConfigDTO dto) {
		return sessionFactory.withTransaction(
			(session, transaction) ->
				findById(session, id)
					.call(config -> Mutiny.fetch(config.getVisibleFields()))
					.flatMap(config -> {
						var newStateConfig = mapper.update(config, dto);
						newStateConfig.setEnabled(
							Boolean.TRUE.equals(dto.getEnabled()));

						return findVisibleFields(session, dto.getVisibleFieldIds())
							.flatMap(visibleFields -> {
								replaceVisibleFields(newStateConfig, visibleFields);

								return session.merge(newStateConfig);
							});
					})
		);
	}

	@Override
	public Uni<ResultAclConfig> patch(long id, ResultAclConfigDTO dto) {
		return sessionFactory.withTransaction(
			(session, transaction) ->
				findById(session, id)
					.call(config -> Mutiny.fetch(config.getVisibleFields()))
					.flatMap(config -> {
						var newStateConfig = mapper.patch(config, dto);

						if (dto.getVisibleFieldIds() == null) {
							return session.merge(newStateConfig);
						}

						return findVisibleFields(session, dto.getVisibleFieldIds())
							.flatMap(visibleFields -> {
								replaceVisibleFields(newStateConfig, visibleFields);

								return session.merge(newStateConfig);
							});
					})
		);
	}

	/**
	 * Replaces the visible fields of a persisted configuration, changing its
	 * set in place: the cache invalidation reacts to an updated collection,
	 * not to a collection replaced by a new one.
	 *
	 * @param config        the persisted configuration
	 * @param visibleFields the new visible fields
	 */
	private static void replaceVisibleFields(
		ResultAclConfig config, Set<DocTypeField> visibleFields) {

		config.getVisibleFields().clear();
		config.getVisibleFields().addAll(visibleFields);
	}

	/**
	 * Returns the DocTypeFields still visible on a result the user is not
	 * authorized to read.
	 *
	 * @param id the ID of the configuration
	 * @return the visible fields
	 */
	public Uni<Set<DocTypeField>> getVisibleFields(long id) {
		return sessionFactory.withTransaction(
			(session, transaction) ->
				findById(session, id)
					.flatMap(config -> Mutiny.fetch(config.getVisibleFields()))
		);
	}

	/**
	 * Reads what a search of the given tenant needs of its configuration.
	 * <p>
	 * The visible fields are limited to the ones that can be visible, see
	 * {@link ResultAclConfig#selectableFieldIds}: a field accepted when the
	 * configuration was saved may stop being selectable afterwards, for
	 * instance when it is excluded, or when a NESTED field gets sub-fields
	 * and would show a whole subtree.
	 *
	 * @param tenantId the tenant to read the configuration of
	 * @return the settings, {@link ResultAclConfig.SearchSettings#DISABLED}
	 *         when the tenant has no configuration or the mode is disabled
	 */
	public Uni<ResultAclConfig.SearchSettings> findSearchSettings(
		String tenantId) {

		return sessionFactory.withTransaction(tenantId, (session, transaction) ->
			findCurrent(session).flatMap(config -> {
				if (config == null || !config.isEnabled()) {
					return Uni.createFrom().item(
						ResultAclConfig.SearchSettings.DISABLED);
				}

				return Mutiny.fetch(config.getVisibleFields())
					.flatMap(visibleFields -> docTypeFieldService
						.findResultAclSelectableIds(session)
						.map(selectableIds -> new ResultAclConfig.SearchSettings(
							true,
							Set.copyOf(ResultAclConfig.toRoleList(
								config.getSuperUserRoles())),
							visibleFields.stream()
								.map(DocTypeField::getId)
								.filter(selectableIds::contains)
								.collect(Collectors.toUnmodifiableSet())
						)));
			})
		);
	}

	private Uni<Set<DocTypeField>> findVisibleFields(
		Mutiny.Session session, Set<Long> visibleFieldIds) {

		if (visibleFieldIds == null || visibleFieldIds.isEmpty()) {
			return Uni.createFrom().item(new LinkedHashSet<>());
		}

		return docTypeFieldService.findByIds(session, visibleFieldIds)
			.flatMap(fields -> docTypeFieldService
				.findResultAclSelectableIds(session)
				.map(selectableIds -> {
					validateVisibleFields(visibleFieldIds, fields, selectableIds);

					return new LinkedHashSet<>(fields);
				}));
	}

	/**
	 * Checks that every requested field exists and can be visible.
	 * <p>
	 * A visible field is the most specific field holding a value of the
	 * document, see {@link ResultAclConfig#selectableFieldIds}: an excluded
	 * field is never part of the documents returned by a search, and a
	 * container, a variant of an I18N field or a multi-field would show
	 * either a whole subtree or nothing.
	 *
	 * @param requestedIds    the field IDs requested in the DTO
	 * @param persistedFields the fields found on the database, with a
	 *                        {@code null} entry for each missing ID
	 * @param selectableIds   the IDs of the fields that can be visible
	 * @throws InvalidDocTypeFieldSetException if an ID does not exist or a
	 *                                         field cannot be visible
	 */
	private static void validateVisibleFields(
		Set<Long> requestedIds, List<DocTypeField> persistedFields,
		Set<Long> selectableIds) {

		var existingFields = persistedFields.stream()
			.filter(Objects::nonNull)
			.toList();

		var persistedIds = existingFields.stream()
			.map(DocTypeField::getId)
			.collect(Collectors.toSet());

		if (!persistedIds.equals(requestedIds)) {
			throw new InvalidDocTypeFieldSetException(
				"One or more visible fields do not match the persisted "
				+ "docTypeFields");
		}

		if (!selectableIds.containsAll(requestedIds)) {
			throw new InvalidDocTypeFieldSetException(
				"One or more docTypeFields cannot be visible fields: excluded "
				+ "fields, OBJECT fields, containers with sub-fields, variants "
				+ "of an i18n field and multi-fields are not allowed");
		}
	}

}
