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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.validation.ValidationException;

import io.openk9.datasource.mapper.RAGConfigurationMapper;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Bucket_;
import io.openk9.datasource.model.RAGConfiguration;
import io.openk9.datasource.model.RAGConfiguration_;
import io.openk9.datasource.model.RAGType;
import io.openk9.datasource.model.dto.base.RAGConfigurationDTO;
import io.openk9.datasource.model.dto.request.CreateRAGConfigurationDTO;

import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
public class RAGConfigurationService
	extends BaseK9EntityService<RAGConfiguration, RAGConfigurationDTO> {

	RAGConfigurationService(RAGConfigurationMapper mapper) {
		this.mapper = mapper;
	}

	/**
	 * Returns the corresponding field name in the {@link Bucket} entity for the given {@link RAGType}.
	 *
	 * <p>This method maps each {@link RAGType} to its respective field in the {@link Bucket} entity,
	 * which is used to determine the association between a Bucket and a RAGConfiguration.</p>
	 *
	 * @param ragType The {@link RAGType} for which to retrieve the corresponding field name.
	 * @return The name of the field in {@link Bucket} that represents the specified {@link RAGType}.
	 */
	private static String getRelevantBucketField(RAGType ragType) {
		return switch (ragType) {
			case CHAT_RAG -> Bucket_.RAG_CONFIGURATION_CHAT;
			case CHAT_RAG_TOOL -> Bucket_.RAG_CONFIGURATION_CHAT_TOOL;
			case SIMPLE_GENERATE -> Bucket_.RAG_CONFIGURATION_SIMPLE_GENERATE;
		};
	}

	/**
	 * Creates a new {@link RAGConfiguration} using the provided Mutiny session.
	 * Converts the given DTO into a transient entity, and persists it using the session.
	 * <p>
	 * Error Handling: Fails with {@link ValidationException} if the type is missing.
	 *
	 * @param session the session to use for persistence
	 * @param dto the DTO containing configuration data
	 * @return a {@link Uni} emitting the created {@link RAGConfiguration}
	 */
	public Uni<RAGConfiguration> create(Mutiny.Session session, RAGConfigurationDTO dto) {
		try {
			var entity = createTransient(dto);
			return super.create(session, entity);
		}
		catch (ValidationException e) {
			return Uni.createFrom().failure(e);
		}
	}

	/**
	 * Creates a new {@link RAGConfiguration} within a managed transaction.
	 * Converts the DTO into a transient entity, and persists it transactionally.
	 * <p>
	 * Error Handling: Fails with {@link ValidationException} if the type is missing.
	 *
	 * @param dto the DTO containing configuration data
	 * @return a {@link Uni} emitting the created {@link RAGConfiguration}
	 */
	public Uni<RAGConfiguration> create(RAGConfigurationDTO dto) {
		try {
			var entity = createTransient(dto);
			return sessionFactory.withTransaction(
				(s, transaction) -> super.create(s, entity)
			);
		}
		catch (ValidationException e) {
			return Uni.createFrom().failure(e);
		}
	}

	/**
	 * Retrieves a list of {@link RAGConfiguration} entities that match the given {@code ragType}
	 * and are not yet associated with the specified Bucket.
	 *
	 * <p>This method executes a database query to find {@link RAGConfiguration} instances
	 * that meet the following conditions:
	 * <ul>
	 *   <li>They have the specified {@code ragType}.</li>
	 *   <li>They are not already linked to the given {@code bucketId}.</li>
	 * </ul>
	 *
	 * @param bucketId The ID of the Bucket to check for associated {@link RAGConfiguration} entities.
	 * @param ragType The type of RAGConfiguration to filter by.
	 * @return A {@link Uni} containing a list of {@link RAGConfiguration} entities
	 *         that match the criteria.
	 */
	public Uni<List<RAGConfiguration>> findUnboundRAGConfigurationByBucket(long bucketId, RAGType ragType) {
		return sessionFactory.withTransaction(s -> {
			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			CriteriaQuery<RAGConfiguration> query = cb.createQuery(RAGConfiguration.class);
			Root<RAGConfiguration> rootRAGConfiguration = query.from(RAGConfiguration.class);

			Subquery<Long> subquery = query.subquery(Long.class);
			Root<Bucket> subRootBucket = subquery.from(Bucket.class);

			subquery.select(subRootBucket.get(Bucket_.id));

			String relevantBucketField = getRelevantBucketField(ragType);
			Path<RAGConfiguration> ragTypePathInBucket = subRootBucket.get(relevantBucketField);

			Predicate bucketIdMatch = cb.equal(subRootBucket.get(Bucket_.id), bucketId);
			Predicate ragTypeMatchSubquery =
				cb.equal(ragTypePathInBucket, rootRAGConfiguration);
			Predicate ragTypeIsNotNull = cb.isNotNull(ragTypePathInBucket);

			subquery.where(cb.and(bucketIdMatch, ragTypeMatchSubquery, ragTypeIsNotNull));

			Predicate ragTypeMatch =
				cb.equal(rootRAGConfiguration.get(RAGConfiguration_.TYPE), ragType);

			Predicate notAssociated = cb.not(cb.exists(subquery));

			query.where(cb.and(ragTypeMatch, notAssociated));
			query.select(rootRAGConfiguration);

			return s.createQuery(query).getResultList();
		});
	}

	@Override
	public Class<RAGConfiguration> getEntityClass() {
		return RAGConfiguration.class;
	}

	/**
	 * Converts a {@link CreateRAGConfigurationDTO} into a non-persistent {@link RAGConfiguration} entity.
	 * <p>
	 * This utility method maps the input DTO to a new entity instance and ensures
	 * that the mandatory {@code type} field is present.
	 * <p>
	 * Error Handling: Throws {@link ValidationException} if the DTO is invalid or the type is null.
	 *
	 * @param dto the DTO used for creating the entity
	 * @return a new {@link RAGConfiguration} instance ready to be persisted
	 * @throws ValidationException if the DTO is not of the expected type or lacks a type value
	 */
	private RAGConfiguration createTransient(RAGConfigurationDTO dto)
		throws ValidationException {

		if (dto instanceof CreateRAGConfigurationDTO createRAGConfigurationDTO
				&& createRAGConfigurationDTO.getType() != null) {

			var transientRagConfiguration = mapper.create(createRAGConfigurationDTO);
			transientRagConfiguration.setType(createRAGConfigurationDTO.getType());

			return transientRagConfiguration;
		}

		throw new ValidationException("Missing RAG type.");
	}
}
