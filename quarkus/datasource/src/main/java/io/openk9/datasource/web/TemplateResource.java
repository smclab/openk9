package io.openk9.datasource.web;


import io.openk9.datasource.model.DataIndex_;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeTemplate;
import io.openk9.datasource.model.DocType_;
import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.model.Tenant_;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@Path("/v1/template")
public class TemplateResource {


	@Path("/get/full/{virtualhost}")
	@POST
	public Uni<List<DocTypeTemplate>> getTemplates(@PathParam("virtualhost") String virtualhost) {

		return getDocTypeTemplateList(virtualhost);

	}

	@Path("/get/id/{virtualhost}")
	@POST
	public Uni<List<Long>> getTemplatesIds(@PathParam("virtualhost") String virtualhost) {

		List<Long> templatesIds = new ArrayList<>();

		getDocTypeTemplateList(virtualhost).invoke(docTypeTemplates -> {
			for (DocTypeTemplate docTypeTemplate : docTypeTemplates) {
				templatesIds.add(docTypeTemplate.getId());
			}

		});

		return Uni.createFrom().item(templatesIds);

	}

	private Uni<List<DocTypeTemplate>> getDocTypeTemplateList(String virtualhost) {
		return sf.withTransaction(session -> {

			CriteriaBuilder cb = sf.getCriteriaBuilder();

			CriteriaQuery<DocTypeTemplate> query = cb.createQuery(DocTypeTemplate.class);

			Root<Tenant> from = query.from(Tenant.class);

			Join<DocType, DocTypeTemplate> fetch =
				from.join(Tenant_.datasources)
					.join(Datasource_.dataIndex)
					.join(DataIndex_.docTypes)
					.join(DocType_.docTypeTemplate);

			query.select(fetch);

			query.where(cb.equal(from.get(Tenant_.virtualHost), virtualhost));

			return session.createQuery(query).getResultList();

		});

	}

	@Inject
	Mutiny.SessionFactory sf;

}
