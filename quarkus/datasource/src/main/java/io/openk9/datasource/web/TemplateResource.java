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
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.List;

@ApplicationScoped
@Path("/v1/template")
public class TemplateResource {


	@Path("/get/{virtualhost}")
	@POST
	public Uni<List<DocTypeTemplate>> getTemplates(@PathParam("virtualhost") String virtualhost) {

		return getDocTypeTemplateList(virtualhost);

	}

	private Uni<List<DocTypeTemplate>> getDocTypeTemplateList(String virtualhost) {

		return sf.withTransaction(session -> {

			CriteriaBuilder cb = sf.getCriteriaBuilder();

			CriteriaQuery<DocTypeTemplate> query = cb.createQuery(DocTypeTemplate.class);

				Root<Tenant> from = query.from(Tenant.class);

				Fetch<DocType, DocTypeTemplate> fetch =
					from.fetch(Tenant_.datasources, JoinType.LEFT)
						.fetch(Datasource_.dataIndex, JoinType.LEFT)
						.fetch(DataIndex_.docTypes, JoinType.LEFT)
						.fetch(DocType_.docTypeTemplate);

				query.select((Selection<? extends DocTypeTemplate>) fetch);

				query.where(cb.equal(from.get(Tenant_.virtualHost), virtualhost));

				return session.createQuery(query).getResultList();

			}
		);

	}


	@Inject
	Mutiny.SessionFactory sf;

}
