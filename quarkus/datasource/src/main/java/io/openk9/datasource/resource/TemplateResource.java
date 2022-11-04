package io.openk9.datasource.resource;

import io.openk9.datasource.model.DocTypeTemplate;
import io.openk9.datasource.model.dto.DocTypeTemplateDTO;
import io.openk9.datasource.resource.util.BaseK9EntityResource;
import io.openk9.datasource.service.DocTypeTemplateService;

import javax.ws.rs.Path;

@Path("/templates")
public class TemplateResource extends
	BaseK9EntityResource<DocTypeTemplateService, DocTypeTemplate, DocTypeTemplateDTO> {

	protected TemplateResource(DocTypeTemplateService service) {
		super(service);
	}

}
