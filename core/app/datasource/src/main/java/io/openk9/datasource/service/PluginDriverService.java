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
import io.openk9.common.util.FieldValidator;
import io.openk9.common.util.Response;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.actor.EventBusInstanceHolder;
import io.openk9.datasource.index.IndexMappingService;
import io.openk9.datasource.mapper.IngestionPayloadMapper;
import io.openk9.datasource.mapper.PluginDriverMapper;
import io.openk9.datasource.model.AclMapping;
import io.openk9.datasource.model.AclMapping_;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.DocTypeField_;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.PluginDriverDocTypeFieldKey;
import io.openk9.datasource.model.PluginDriverDocTypeFieldKey_;
import io.openk9.datasource.model.PluginDriver_;
import io.openk9.datasource.model.UserField;
import io.openk9.datasource.model.dto.base.PluginDriverDTO;
import io.openk9.datasource.model.dto.request.PluginWithDocTypeDTO;
import io.openk9.datasource.model.form.FormTemplate;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.model.util.K9Entity_;
import io.openk9.datasource.plugindriver.HttpPluginDriverClient;
import io.openk9.datasource.plugindriver.HttpPluginDriverInfo;
import io.openk9.datasource.resource.util.Filter;
import io.openk9.datasource.resource.util.Page;
import io.openk9.datasource.resource.util.Pageable;
import io.openk9.datasource.service.util.Tuple2;
import io.openk9.datasource.web.dto.PluginDriverDocTypesDTO;
import io.openk9.datasource.web.dto.HealthDTO;
import io.openk9.datasource.web.dto.ResourceUriDTO;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.SetJoin;
import jakarta.persistence.criteria.Subquery;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class PluginDriverService
	extends BaseK9EntityService<PluginDriver, PluginDriverDTO> {

	private static final Logger log = Logger.getLogger(PluginDriverService.class);
	@Inject
	DocTypeFieldService docTypeFieldService;
	@Inject
	DocTypeService docTypeService;
	@Inject
	HttpPluginDriverClient httpPluginDriverClient;
	@Inject
	IndexMappingService indexMappingService;

	PluginDriverService(PluginDriverMapper mapper) {
		this.mapper = mapper;
	}

	public Uni<Tuple2<PluginDriver, DocTypeField>> addDocTypeField(
		long pluginDriverId, long docTypeFieldId, UserField userField) {

		return sessionFactory.withTransaction((s) -> findById(s, pluginDriverId)
			.onItem()
			.ifNotNull()
			.transformToUni(pluginDriver ->
				docTypeFieldService.findById(s, docTypeFieldId)
					.onItem()
					.ifNotNull()
					.transformToUni(docTypeField -> s
						.fetch(pluginDriver.getAclMappings())
						.flatMap(aclMappings -> {

							AclMapping newAclMapping =
								AclMapping.of(
									PluginDriverDocTypeFieldKey.of(
										pluginDriverId, docTypeFieldId),
									pluginDriver, docTypeField, userField
								);

							if (aclMappings.add(newAclMapping)) {
								pluginDriver.setAclMappings(aclMappings);
								return persist(s, pluginDriver).map(ep -> Tuple2.of(
									ep,
									docTypeField
								));
							}
							else {
								return Uni.createFrom().nullItem();
							}

						})
					)
			)
		);
	}

	public Uni<Set<DocType>> createPluginDriverDocTypes(long id) {
		return sessionFactory.withTransaction(session -> findById(id)
			.flatMap(pluginDriver ->
				indexMappingService.generateDocTypeFieldsFromPluginDriverSampleSync(
					session,
					pluginDriver.getHttpPluginDriverInfo()
				)
			)
		);
	}

	public Uni<Response<PluginDriver>> createWithDocType(
		PluginWithDocTypeDTO dto) {

		return sessionFactory.withTransaction(
			(session, transaction) -> {
				var constraintViolations = validator.validate(dto);

				if (!constraintViolations.isEmpty()) {
					var fieldValidators = constraintViolations.stream()
						.map(constraintViolation -> FieldValidator.of(
							constraintViolation.getPropertyPath().toString(),
							constraintViolation.getMessage()))
						.collect(Collectors.toList());

					return Uni.createFrom().item(Response.of(null, fieldValidators));
				}

				return createWithDocType(session, dto)
					.flatMap(pluginDriver ->
						Uni.createFrom().item(Response.of(pluginDriver,null)));
			}
		);
	}

	public Uni<PluginDriver> createWithDocType(Mutiny.Session s, PluginWithDocTypeDTO dto) {

		var transientPluginDriver = mapper.create(dto);

		return super.create(s, transientPluginDriver)
			.flatMap(pluginDriver -> {
				var aclMappings = new LinkedHashSet<AclMapping>();

				for (PluginWithDocTypeDTO.DocTypeUserDTO docTypeUser
						: dto.getDocTypeUserDTOSet()) {

					var docTypeField =
						s.getReference(DocTypeField.class, docTypeUser.getDocTypeId());

					var aclMapping = new AclMapping();
					aclMapping.setPluginDriver(pluginDriver);
					aclMapping.setDocTypeField(docTypeField);
					aclMapping.setUserField(docTypeUser.getUserField());

					var key = PluginDriverDocTypeFieldKey.of(
						pluginDriver.getId(), docTypeUser.getDocTypeId());

					aclMapping.setKey(key);

					aclMappings.add(aclMapping);
				}

				pluginDriver.setAclMappings(aclMappings);

				return s
					.persist(pluginDriver)
					.flatMap(__ -> s.merge(pluginDriver));
			});
	}

	public Uni<Set<AclMapping>> getAclMappings(PluginDriver pluginDriver) {
		return sessionFactory.withTransaction(s -> s
			.merge(pluginDriver)
			.flatMap(merged -> s.fetch(merged.getAclMappings()))
		);
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
			searchText
		);
	}

	public Uni<Page<DocTypeField>> getDocTypeFields(
		long pluginDriverId, Pageable pageable,
		Filter filter) {

		return findAllPaginatedJoin(
			new Long[]{pluginDriverId},
			PluginDriver_.ACL_MAPPINGS, DocTypeField.class,
			pageable.getLimit(), pageable.getSortBy().name(),
			pageable.getAfterId(), pageable.getBeforeId(),
			filter
		);
	}

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
					.filter(e -> Objects.equals(
						e.getAttribute(),
						PluginDriver_.aclMappings
					))
					.map(e -> (Join<PluginDriver, AclMapping>) e)
					.map(e -> e.get(AclMapping_.userField))
					.map(cb::asc)
					.collect(Collectors.toList())
		);

	}

	public Uni<Set<DocTypeField>> getDocTypeFieldsInPluginDriver(
		long pluginDriverId) {

		return sessionFactory.withTransaction(
			s ->
				findById(s, pluginDriverId)
					.flatMap(
						ep -> s.fetch(ep.getAclMappings()))
					.map(l -> l
						.stream()
						.map(AclMapping::getDocTypeField)
						.collect(Collectors.toSet()))
		);

	}

	public Uni<List<DocTypeField>> getDocTypeFieldsNotInPluginDriver(
		long pluginDriverId) {

		return sessionFactory.withTransaction(
			s -> {

				CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

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

	public Uni<PluginDriverDocTypesDTO> getDocTypes(long id) {
		return sessionFactory.withSession(session -> findById(id)
			.flatMap(pluginDriver ->
				httpPluginDriverClient.getSample(pluginDriver.getHttpPluginDriverInfo()))

			.map(IngestionPayloadMapper::getDocumentTypes)
			.flatMap(docTypeNames -> {
				var mutableSet = new HashSet<>(docTypeNames);
				mutableSet.add(DocType.DEFAULT_NAME);
				var docTypeNameValues = mutableSet.toArray(String[]::new);

				return docTypeService.getDocTypesInDocTypeNames(
						session, docTypeNameValues)
					.map(PluginDriverDocTypesDTO::selectedDocTypes)
					.flatMap(selected -> docTypeService
						.getDocTypesNotInDocTypeNames(session, docTypeNameValues)
						.map(PluginDriverDocTypesDTO::unselectedDocTypes)
						.map(unselected -> PluginDriverDocTypesDTO.join(selected, unselected))
					);
			})
		);
	}

	@Override
	public Class<PluginDriver> getEntityClass() {
		return PluginDriver.class;
	}

	public Uni<FormTemplate> getForm(long id) {
		return findById(id)
			.flatMap(pluginDriver -> {
                        HttpPluginDriverInfo httpPluginDriverInfo = pluginDriver.getHttpPluginDriverInfo();
                        ResourceUriDTO resourceUriDTO = ResourceUriDTO.builder()
                                .baseUri(httpPluginDriverInfo.getBaseUri())
                                .path(httpPluginDriverInfo.getPath())
                                .build();
                        return httpPluginDriverClient.getForm(resourceUriDTO);
                    }
			);
	}

	public Uni<HealthDTO> getHealth(PluginDriverDTO pluginDriverDTO) {
        HttpPluginDriverInfo httpPluginDriverInfo = PluginDriver.parseHttpInfo(pluginDriverDTO.getJsonConfig());
        ResourceUriDTO resourceUriDTO = ResourceUriDTO.builder()
                .baseUri(httpPluginDriverInfo.getBaseUri())
                .path(httpPluginDriverInfo.getPath())
                .build();
		return httpPluginDriverClient.getHealth(resourceUriDTO);
	}

	public Uni<HealthDTO> getHealth(long id) {
		return findById(id)
			.flatMap(pluginDriver -> {
                HttpPluginDriverInfo httpPluginDriverInfo = pluginDriver.getHttpPluginDriverInfo();
                ResourceUriDTO resourceUriDTO = ResourceUriDTO.builder()
                        .baseUri(httpPluginDriverInfo.getBaseUri())
                        .path(httpPluginDriverInfo.getPath())
                        .build();
                return httpPluginDriverClient.getHealth(resourceUriDTO);
                    }
			);
	}

	@Override
	public String[] getSearchFields() {
		return new String[]{
			PluginDriver_.NAME, PluginDriver_.DESCRIPTION, PluginDriver_.TYPE
		};
	}

	public Uni<Response<PluginDriver>> patchOrUpdateWithDocType(
		Long pluginId, PluginWithDocTypeDTO dto, boolean patch) {



		return sessionFactory.withTransaction(
			(session, transaction) -> {
				var constraintViolations = validator.validate(dto);

				if ( !constraintViolations.isEmpty() ) {
					var fieldValidators = constraintViolations.stream()
						.map(constraintViolation -> FieldValidator.of(
							constraintViolation.getPropertyPath().toString(),
							constraintViolation.getMessage()))
						.collect(Collectors.toList());

					return Uni.createFrom().item(Response.of(null, fieldValidators));
				}

				return patchOrUpdateWithDocType(session, pluginId, dto, patch)
					.flatMap(pluginDriver ->
						Uni.createFrom().item(Response.of(pluginDriver, null)));
			});
	}

	public Uni<PluginDriver> patchOrUpdateWithDocType(
		Mutiny.Session s, Long pluginId, PluginWithDocTypeDTO dto, boolean patch) {

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaDelete<AclMapping> deleteAclMapping =
			cb.createCriteriaDelete(AclMapping.class);
		Root<AclMapping> deleteFrom =
			deleteAclMapping.from(AclMapping.class);

		var docTypeUserDTOSet = dto.getDocTypeUserDTOSet();

		return findById(s, pluginId)
			.call(plugin -> Mutiny.fetch(plugin.getAclMappings()))
			.call(plugin -> {

				var pluginIdPath =
					deleteFrom.get(AclMapping_.pluginDriver).get(PluginDriver_.id);
				var docTypeIdPath =
					deleteFrom.get(AclMapping_.docTypeField).get(DocTypeField_.id);

				if ( docTypeUserDTOSet == null || docTypeUserDTOSet.isEmpty() ) {
					if ( patch ) {
						return Uni.createFrom().item(plugin);
					}
					else {
						deleteAclMapping.where(pluginIdPath.in(pluginId));

						//removes aclMapping old list
						return s.createQuery(deleteAclMapping).executeUpdate()
							.map(v -> plugin);
					}
				}
				else {
					//retrieves docType ids to keep
					var docTypeIdsToKeep = docTypeUserDTOSet.stream()
						.map(PluginWithDocTypeDTO.DocTypeUserDTO::getDocTypeId)
						.collect(Collectors.toSet());

					deleteAclMapping.where(
						cb.and(
							pluginIdPath.in(pluginId),
							cb.not(docTypeIdPath.in(docTypeIdsToKeep))
						));

					//removes aclMapping old list
					return s.createQuery(deleteAclMapping).executeUpdate()
						.map(v -> plugin);
				}
			})
			.call(Mutiny::fetch)
			.onItem().ifNotNull()
			.transformToUni(plugin -> {

				PluginDriver newStatePluginDriver;
				var newHashSet = new HashSet<AclMapping>();

				if ( patch ) {
					newStatePluginDriver = mapper.patch(plugin, dto);
				}
				else {
					newStatePluginDriver = mapper.patch(plugin, dto);
					newStatePluginDriver.setAclMappings(newHashSet);
				}

				//set new aclMapping Set
				if ( docTypeUserDTOSet != null ) {
					newStatePluginDriver.setAclMappings(newHashSet);

					docTypeUserDTOSet.forEach(docTypeUserDTO -> {
						var docTypeId = docTypeUserDTO.getDocTypeId();
						var key =
							PluginDriverDocTypeFieldKey.of(pluginId, docTypeId);
						var docTypeReference =
							s.getReference(DocTypeField.class, docTypeId);

						var aclMapping = new AclMapping();
						aclMapping.setPluginDriver(plugin);
						aclMapping.setDocTypeField(docTypeReference);
						aclMapping.setKey(key);
						aclMapping.setUserField(docTypeUserDTO.getUserField());

						newStatePluginDriver.getAclMappings().add(aclMapping);
					});
				}

				return s.merge(newStatePluginDriver)
					.map(v -> newStatePluginDriver)
					.call(s::flush);
			});
	}

	@Override
	public <T extends K9Entity> Uni<T> persist(Mutiny.Session session, T entity) {

		return super.persist(session, entity)
			.log("Trying to create PluginDriver.")
			.log("Creating DocumentTypes associated with pluginDriver")
			.call(() -> {
				var pluginDriver = (PluginDriver) entity;

				return switch (pluginDriver.getProvisioning()) {
					case USER -> indexMappingService.generateDocTypeFieldsFromPluginDriverSampleSync(
							session,
							pluginDriver.getHttpPluginDriverInfo()
						)
						.onItem()
						.invoke(() -> log.info("DocumentTypes associated with pluginDriver created."))
						.onFailure()
						.invoke(throwable -> {
							if (log.isDebugEnabled()) {
								log.debug("Error creating/updating DocumentTypes associated with pluginDriver", throwable);
							}
							else {
								log.warn("Error creating/updating DocumentTypes associated with pluginDriver");
							}
						});
					case SYSTEM -> getCurrentTenant(session)
						.flatMap(tenant -> {
							// fire and forget using the eventBus message
							EventBusInstanceHolder.getEventBus()
								.send(
									IndexMappingService.GENERATE_DOC_TYPE,
									new IndexMappingService.GenerateDocTypeFromPluginSampleMessage(
										tenant.schemaName(),
										pluginDriver.getHttpPluginDriverInfo()
									)
								);
							return Uni.createFrom().item(entity);
						});
				};
			});
	}

	public Uni<Tuple2<PluginDriver, DocTypeField>> removeDocTypeField(
		long pluginDriverId,
		long docTypeFieldId) {

		return sessionFactory.withTransaction((s) -> findById(s, pluginDriverId)
			.onItem()
			.ifNotNull()
			.transformToUni(pluginDriver ->
				docTypeFieldService.findById(s, docTypeFieldId)
					.onItem()
					.ifNotNull()
					.transformToUni(docTypeField -> s
						.fetch(pluginDriver.getAclMappings())
						.flatMap(aclMappings -> {

							boolean removed = aclMappings.removeIf(
								epi -> epi.getKey().getDocTypeFieldId() == docTypeFieldId
									   && epi.getKey().getPluginDriverId() == pluginDriverId);

							if (removed) {
								return s.find(
										AclMapping.class,
										PluginDriverDocTypeFieldKey.of(pluginDriverId, docTypeFieldId)
									)
									.call(s::remove)
									.map(ep -> Tuple2.of(pluginDriver, docTypeField));
							}
							else {
								return Uni.createFrom().nullItem();
							}

						}))));
	}

	public Uni<AclMapping> setUserField(
		long pluginDriverId,
		long docTypeFieldId, UserField userField) {

		return sessionFactory.withTransaction(s -> {
			CriteriaBuilder criteria = sessionFactory.getCriteriaBuilder();

			CriteriaUpdate<AclMapping> query =
				criteria.createCriteriaUpdate(AclMapping.class);

			Root<AclMapping> from = query.from(AclMapping.class);

			query.where(criteria.and(
				criteria.equal(from
					.get(AclMapping_.key)
					.get(PluginDriverDocTypeFieldKey_.pluginDriverId), pluginDriverId),
				criteria.equal(from
					.get(AclMapping_.key)
					.get(PluginDriverDocTypeFieldKey_.docTypeFieldId), docTypeFieldId),
				criteria.notEqual(from.get(AclMapping_.userField), userField)
			));

			query.set(from.get(AclMapping_.userField), userField);

			return s.createQuery(query).executeUpdate().call(s::flush).flatMap(rowCount -> {
				if (rowCount == 0) {
					return Uni.createFrom().nullItem();
				}
				return s.find(
					AclMapping.class,
					PluginDriverDocTypeFieldKey.of(pluginDriverId, docTypeFieldId)
				);
			});
		});
	}

	@Override
	protected <T extends K9Entity> Uni<T> merge(Mutiny.Session s, T entity) {
		return super.merge(s, entity)
			.log("Trying to update PluginDriver.")
			.log("Updating DocumentTypes associated with pluginDriver")
			.call(() -> {
				PluginDriver pluginDriver = (PluginDriver) entity;

				return switch (pluginDriver.getProvisioning()) {
					case USER -> indexMappingService.generateDocTypeFieldsFromPluginDriverSampleSync(
							s,
							pluginDriver.getHttpPluginDriverInfo()
						)
						.onItem()
						.invoke(() -> log.info("DocumentTypes associated with pluginDriver updated."))
						.onFailure()
						.invoke(throwable -> {
							if (log.isDebugEnabled()) {
								log.debug("Error creating/updating DocumentTypes associated with pluginDriver", throwable);
							}
							else {
								log.warn("Error creating/updating DocumentTypes associated with pluginDriver");
							}
						});
					case SYSTEM -> getCurrentTenant(s)
						.flatMap(tenant -> {
							// fire and forget using the eventBus message
							EventBusInstanceHolder.getEventBus()
								.send(
									IndexMappingService.GENERATE_DOC_TYPE,
									new IndexMappingService.GenerateDocTypeFromPluginSampleMessage(
										tenant.schemaName(),
										pluginDriver.getHttpPluginDriverInfo()
									)
								);
							return Uni.createFrom().item(entity);
						});
				};
			});
	}

}
