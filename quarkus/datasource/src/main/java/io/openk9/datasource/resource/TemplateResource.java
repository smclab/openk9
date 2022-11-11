package io.openk9.datasource.resource;

import io.openk9.datasource.model.DocTypeTemplate;
import io.openk9.datasource.model.dto.DocTypeTemplateDTO;
import io.openk9.datasource.resource.util.BaseK9EntityResource;
import io.openk9.datasource.service.DocTypeTemplateService;
import io.smallrye.mutiny.Uni;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/templates")
public class TemplateResource extends
	BaseK9EntityResource<DocTypeTemplateService, DocTypeTemplate, DocTypeTemplateDTO> {

	protected TemplateResource(DocTypeTemplateService service) {
		super(service);
	}

	@GET
	@Path("/{id}/compiled")
	public Uni<String> getTemplateCompiled(@PathParam("id") long docTypeTemplateId) {

		return service.findById(docTypeTemplateId).map(
			DocTypeTemplate::getCompiled);
	}

}
