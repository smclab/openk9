/*
 * Copyright (C) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
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

import io.openk9.datasource.mapper.AutocorrectionMapper;
import io.openk9.datasource.model.Autocorrection;
import io.openk9.datasource.model.Autocorrection_;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Bucket_;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.dto.base.AutocorrectionDTO;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;

@ApplicationScoped
public class AutocorrectionService extends BaseK9EntityService<Autocorrection, AutocorrectionDTO>{

	AutocorrectionService(AutocorrectionMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public Uni<Autocorrection> create(Autocorrection entity) {
		return sessionFactory.withTransaction(
			(session, transaction) -> create(session, entity)
		);
	}

	@Override
	public Uni<Autocorrection> create(AutocorrectionDTO dto) {
		if (dto.getAutocorrectionDocTypeFieldId() == null || dto.getAutocorrectionDocTypeFieldId() == 0L) {
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

	public Uni<List<Autocorrection>> findUnboundAutocorrectionByBucket(long bucketId) {
		return sessionFactory.withTransaction(s -> {
			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			CriteriaQuery<Autocorrection> query = cb.createQuery(Autocorrection.class);
			Root<Autocorrection> rootAutocorrection = query.from(Autocorrection.class);

			Subquery<Long> subquery = query.subquery(Long.class);
			Root<Bucket> subRootBucket = subquery.from(Bucket.class);

			var associatedAutocorrectionPath = subRootBucket.get(Bucket_.autocorrection);

			subquery.select(associatedAutocorrectionPath.get(Autocorrection_.id));

			subquery.where(
				cb.and(
					cb.equal(subRootBucket.get(Bucket_.id), bucketId),
					cb.isNotNull(associatedAutocorrectionPath)
				)
			);

			query.select(rootAutocorrection);
			query.where(cb.not(rootAutocorrection.get(Autocorrection_.id).in(subquery)));

			return s.createQuery(query).getResultList();
		});
	}

	public Uni<DocTypeField> getAutocorrectionDocTypeField(long autocorrectionId) {
		return sessionFactory.withTransaction(session ->
			findById(session, autocorrectionId)
				.flatMap(autocorrection ->
					session.fetch(autocorrection.getAutocorrectionDocTypeField())
				)
		);
	}

	@Override
	public Class<Autocorrection> getEntityClass() {
		return Autocorrection.class;
	}

	public Uni<Autocorrection> patch(long autocorrectionId, AutocorrectionDTO dto) {
		return sessionFactory.withTransaction(session ->
			findById(autocorrectionId)
				.flatMap(autocorrection -> {
					var newStateAutocorrection = mapper.patch(autocorrection, dto);

					if (dto.getAutocorrectionDocTypeFieldId() != null) {
						DocTypeField docTypeField =
							session.getReference(
								DocTypeField.class,
								dto.getAutocorrectionDocTypeFieldId()
							);

						newStateAutocorrection.setAutocorrectionDocTypeField(docTypeField);
					}
					return session.merge(newStateAutocorrection)
						.map(ignore -> newStateAutocorrection);
				})
		);
	}

	public Uni<Autocorrection> update(long autocorrectionId, AutocorrectionDTO dto) {
		return sessionFactory.withTransaction(session ->
			findById(autocorrectionId)
				.flatMap(autocorrection -> {
					var newStateAutocorrection = mapper.update(autocorrection, dto);
					var docTypeFieldId = dto.getAutocorrectionDocTypeFieldId();

					DocTypeField docTypeField = null;

					if (docTypeFieldId != null) {
						docTypeField = session.getReference(DocTypeField.class, docTypeFieldId);
					}

					newStateAutocorrection.setAutocorrectionDocTypeField(docTypeField);
					return session.merge(newStateAutocorrection);
				})
		);
	}

	private Uni<Autocorrection> createTransient(Mutiny.Session session, AutocorrectionDTO dto) {
		Autocorrection transientAutocorrection = mapper.create(dto);

		transientAutocorrection.setAutocorrectionDocTypeField(
			session.getReference(DocTypeField.class, dto.getAutocorrectionDocTypeFieldId())
		);

		return Uni.createFrom().item(transientAutocorrection);
	}
}
