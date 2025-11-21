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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import io.openk9.common.graphql.SortBy;
import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.datasource.mapper.AutocompleteMapper;
import io.openk9.datasource.model.Autocomplete;
import io.openk9.datasource.model.Autocomplete_;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Bucket_;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.dto.base.AutocompleteDTO;
import io.openk9.datasource.service.exception.InvalidDocTypeFieldSetException;
import io.openk9.datasource.validation.ValidAutocompleteFieldsValidator;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
public class AutocompleteService extends BaseK9EntityService<Autocomplete, AutocompleteDTO>{

	@Inject
	DocTypeFieldService docTypeFieldService;

	AutocompleteService(AutocompleteMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public Uni<Autocomplete> create(Autocomplete entity) {
		return sessionFactory.withTransaction(
			(session, transaction) -> create(session, entity)
		);
	}

	@Override
	public Uni<Autocomplete> create(AutocompleteDTO dto) {
		if (dto.getFieldIds() == null || dto.getFieldIds().isEmpty()) {
			return super.create(dto);
		}
		else {
			return sessionFactory.withTransaction(
				(session, transaction) ->
					createTransient(session, dto)
						.flatMap(autocorrection ->
							create(session, autocorrection)
						)
			);
		}
	}

	public Uni<List<Autocomplete>> findUnboundAutocompleteByBucket(long bucketId) {
		return sessionFactory.withTransaction(s -> {
			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			CriteriaQuery<Autocomplete> query = cb.createQuery(Autocomplete.class);
			Root<Autocomplete> rootAutocomplete = query.from(Autocomplete.class);

			Subquery<Long> subquery = query.subquery(Long.class);
			Root<Bucket> subRootBucket = subquery.from(Bucket.class);

			var associatedAutocompletePath = subRootBucket.get(Bucket_.autocomplete);

			subquery.select(associatedAutocompletePath.get(Autocomplete_.id));

			subquery.where(
				cb.and(
					cb.equal(subRootBucket.get(Bucket_.id), bucketId),
					cb.isNotNull(associatedAutocompletePath)
				)
			);

			query.select(rootAutocomplete);
			query.where(cb.not(rootAutocomplete.get(Autocomplete_.id).in(subquery)));

			return s.createQuery(query).getResultList();
		});
	}

	public Uni<Connection<DocTypeField>> getDocTypeFieldConnection(
		Long autocompleteId, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList, boolean notEqual) {

		return findJoinConnection(
			autocompleteId, Autocomplete_.FIELDS, DocTypeField.class,
			docTypeFieldService.getSearchFields(), after, before, first, last,
			searchText, sortByList, notEqual);
	}

	@Override
	public Class<Autocomplete> getEntityClass() {
		return Autocomplete.class;
	}

	public Uni<Autocomplete> patch(long autocompleteId, AutocompleteDTO dto) {
		return sessionFactory.withTransaction(
			(session, transaction) -> findById(session, autocompleteId)
				.call(autocomplete -> Mutiny.fetch(autocomplete.getFields()))
				.flatMap(autocomplete -> {
					var newStateAutocomplete = mapper.patch(autocomplete, dto);
					var fieldIds = dto.getFieldIds();

					if (fieldIds != null) {
						newStateAutocomplete.getFields().clear();

						if (!fieldIds.isEmpty()) {
							return Multi.createFrom().iterable(fieldIds)
								.onItem().transformToUniAndConcatenate(
									fieldId -> session.find(DocTypeField.class, fieldId)
								)
								.select().where(Objects::nonNull)
								.collect().asSet()
								.invoke(fields -> {
									if (fields.size() != fieldIds.size()) {
										throw new InvalidDocTypeFieldSetException(
											"Some field IDs do not exist"
										);
									}

									if (!ValidAutocompleteFieldsValidator
											.validateAutocompleteFields(fields)) {

										throw new InvalidDocTypeFieldSetException(
											"All fields must be of type autocomplete (search_as_you_type) and must have a parent."
										);
									}
								})
								.map(fields -> {
									newStateAutocomplete.setFields(new HashSet<>(fields));
									return newStateAutocomplete;
								});
						}
					}

					return Uni.createFrom().item(newStateAutocomplete);
				})
		);
	}

	public Uni<Autocomplete> update(long autocompleteId, AutocompleteDTO dto) {
		return sessionFactory.withTransaction(
			(session, transaction) -> findById(session, autocompleteId)
				.call(autocomplete -> Mutiny.fetch(autocomplete.getFields()))
				.flatMap(autocomplete -> {
					var newStateAutocomplete = mapper.update(autocomplete, dto);
					var fieldIds = dto.getFieldIds();

					newStateAutocomplete.getFields().clear();

					if (fieldIds != null) {
						if (!fieldIds.isEmpty()) {
							return Multi.createFrom().iterable(fieldIds)
								.onItem().transformToUniAndConcatenate(
									fieldId -> session.find(DocTypeField.class, fieldId)
								)
								.select().where(Objects::nonNull)
								.collect().asSet()
								.invoke(fields -> {
									if (fields.size() != fieldIds.size()) {
										throw new InvalidDocTypeFieldSetException(
											"Some field IDs do not exist"
										);
									}

									if (!ValidAutocompleteFieldsValidator
											.validateAutocompleteFields(fields)) {

										throw new InvalidDocTypeFieldSetException(
											"All fields must be of type autocomplete (search_as_you_type) and must have a parent."
										);
									}
								})
								.map(fields -> {
									newStateAutocomplete.setFields(new HashSet<>(fields));
									return newStateAutocomplete;
								});
						}
					}

					return Uni.createFrom().item(newStateAutocomplete);
				})
		);
	}

	private Uni<Autocomplete> createTransient(Mutiny.Session session, AutocompleteDTO dto) {
		var transientAutocomplete = mapper.create(dto);

		return Multi.createFrom().iterable(dto.getFieldIds())
			.onItem()
			.transformToUniAndConcatenate(fieldId ->
				session.find(DocTypeField.class, fieldId)
			)
			.collect().asSet()
			.map(fields -> {
				transientAutocomplete.setFields(fields);
				return transientAutocomplete;
			});
	}
}
