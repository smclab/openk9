package io.openk9.datasource.service;

import io.openk9.datasource.mapper.DocTypeTemplateMapper;
import io.openk9.datasource.model.DocTypeTemplate;
import io.openk9.datasource.model.DocTypeTemplate_;
import io.openk9.datasource.model.dto.DocTypeTemplateDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.smallrye.mutiny.Uni;
import org.hibernate.FlushMode;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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
