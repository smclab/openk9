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

import io.openk9.datasource.mapper.DocTypeTemplateMapper;
import io.openk9.datasource.model.DocTypeTemplate;
import io.openk9.datasource.model.DocTypeTemplate_;
import io.openk9.datasource.model.dto.DocTypeTemplateDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.FlushMode;

import java.util.List;

@ApplicationScoped
public class DocTypeTemplateService extends BaseK9EntityService<DocTypeTemplate, DocTypeTemplateDTO> {
	DocTypeTemplateService(DocTypeTemplateMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {DocTypeTemplate_.NAME, DocTypeTemplate_.DESCRIPTION};
	}

	public Uni<DocTypeTemplate> findByName(String name) {
		return sessionFactory.withTransaction((s) -> {
			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
			CriteriaQuery<DocTypeTemplate> cq = cb.createQuery(DocTypeTemplate.class);
			Root<DocTypeTemplate> root = cq.from(DocTypeTemplate.class);
			cq.where(cb.equal(root.get(DocTypeTemplate_.name), name));
			return s.createQuery(cq)
				.setFlushMode(FlushMode.MANUAL)
				.getSingleResultOrNull();
		});
	}

	public Uni<List<DocTypeTemplate>> getDocTypeTemplateListByNames(String[] docTypeTemplateNames) {
		return sessionFactory.withTransaction(s -> {

			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			Class<DocTypeTemplate> entityClass = getEntityClass();

			CriteriaQuery<DocTypeTemplate> query = cb.createQuery(entityClass);

			Root<DocTypeTemplate> from = query.from(entityClass);

			query.where(from.get(DocTypeTemplate_.name).in(List.of(docTypeTemplateNames)));

			return s
				.createQuery(query)
				.getResultList();

		});
	}

	public Uni<Boolean> existsByName(String name) {
		return sessionFactory.withTransaction(s -> {

			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			Class<DocTypeTemplate> entityClass = getEntityClass();

			CriteriaQuery<Long> query = cb.createQuery(Long.class);

			Root<DocTypeTemplate> from = query.from(entityClass);

			query.select(cb.count(from));

			query.where(cb.equal(from.get(DocTypeTemplate_.name), name));

			return s
				.createQuery(query)
				.getSingleResult()
				.map(count -> count > 0);

		});

	}

	@Inject
	DocTypeTemplateMapper _docTypeTemplateMapper;

	@Override
	public Class<DocTypeTemplate> getEntityClass() {
		return DocTypeTemplate.class;
	}

}
