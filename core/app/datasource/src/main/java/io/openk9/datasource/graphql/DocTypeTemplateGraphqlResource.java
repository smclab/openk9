package io.openk9.datasource.graphql;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.Response;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.mapper.DocTypeTemplateMapper;
import io.openk9.datasource.model.DocTypeTemplate;
import io.openk9.datasource.model.dto.DocTypeTemplateDTO;
import io.openk9.datasource.service.DocTypeTemplateService;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class DocTypeTemplateGraphqlResource {

	@Query
	public Uni<Connection<DocTypeTemplate>> getDocTypeTemplates(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return _docTypeTemplateService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	@Query
	public Uni<DocTypeTemplate> getDocTypeTemplate(@Id long id) {
		return _docTypeTemplateService.findById(id);
	}

	public Uni<Response<DocTypeTemplate>> patchDocTypeTemplate(@Id long id, DocTypeTemplateDTO docTypeTemplateDTO) {
		return _docTypeTemplateService.getValidator().patch(id, docTypeTemplateDTO);
	}

	public Uni<Response<DocTypeTemplate>> updateDocTypeTemplate(@Id long id, DocTypeTemplateDTO docTypeTemplateDTO) {
		return _docTypeTemplateService.getValidator().update(id, docTypeTemplateDTO);
	}

	public Uni<Response<DocTypeTemplate>> createDocTypeTemplate(DocTypeTemplateDTO docTypeTemplateDTO) {
		return _docTypeTemplateService.getValidator().create(docTypeTemplateDTO);
	}

	@Mutation
	public Uni<Response<DocTypeTemplate>> docTypeTemplate(
		@Id Long id, DocTypeTemplateDTO docTypeTemplateDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createDocTypeTemplate(docTypeTemplateDTO);
		} else {
			return patch
				? patchDocTypeTemplate(id, docTypeTemplateDTO)
				: updateDocTypeTemplate(id, docTypeTemplateDTO);
		}

	}

	@Mutation
	public Uni<DocTypeTemplate> deleteDocTypeTemplate(@Id long docTypeTemplateId) {
		return _docTypeTemplateService.deleteById(docTypeTemplateId);
	}

	@Subscription
	public Multi<DocTypeTemplate> docTypeTemplateCreated() {
		return _docTypeTemplateService
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<DocTypeTemplate> docTypeTemplateDeleted() {
		return _docTypeTemplateService
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<DocTypeTemplate> docTypeTemplateUpdated() {
		return _docTypeTemplateService
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	@Inject
	DocTypeTemplateService _docTypeTemplateService;

	@Inject
	DocTypeTemplateMapper _docTypeTemplateMapper;

}
