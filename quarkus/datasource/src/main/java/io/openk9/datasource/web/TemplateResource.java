package io.openk9.datasource.web;


import io.openk9.datasource.model.DataIndex_;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeTemplate;
import io.openk9.datasource.model.DocType_;
import io.openk9.datasource.model.Tenant;
import io.openk9.datasource.model.Tenant_;
import io.smallrye.mutiny.Uni;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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

	@Path("/get/{virtualhost}")
	@POST
	public Uni<List<TemplateResponseDto>> getTemplates(@PathParam("virtualhost") String virtualhost) {

		return getDocTypeTemplateList(virtualhost);

	}

	private Uni<List<TemplateResponseDto>> getDocTypeTemplateList(String virtualhost) {
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


			return session.createQuery(query).getResultList().map(docTypeTemplates -> {

				List<TemplateResponseDto> responseDtos = new ArrayList<>();

				for (DocTypeTemplate docTypeTemplate : docTypeTemplates) {

					TemplateResponseDto templateResponseDto = new TemplateResponseDto();
					templateResponseDto.setName(docTypeTemplate.getName());
					templateResponseDto.setId(docTypeTemplate.getId());

					responseDtos.add(templateResponseDto);
				}

				return responseDtos;

			});

		});

	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class TemplateResponseDto {
		private String name;
		private Long id;
	}

	@Inject
	Mutiny.SessionFactory sf;

}
