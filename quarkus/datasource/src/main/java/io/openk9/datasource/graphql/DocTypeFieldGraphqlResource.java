package io.openk9.datasource.graphql;

import io.openk9.datasource.model.Analyzer;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.service.DocTypeFieldService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Source;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class DocTypeFieldGraphqlResource {

	public Uni<Analyzer> analyzer(@Source DocTypeField docTypeField) {
		return _docTypeFieldService.getAnalyzer(docTypeField.getId());
	}

	@Mutation
	public Uni<Tuple2<DocTypeField, Analyzer>> bindAnalyzerToDocTypeField(
		@Id long docTypeFieldId, @Id long analyzerId) {
		return _docTypeFieldService.bindAnalyzer(docTypeFieldId, analyzerId);
	}

	@Mutation
	public Uni<Tuple2<DocTypeField, Analyzer>> unbindAnalyzerFromDocTypeField(
		@Id long docTypeFieldId) {
		return _docTypeFieldService.unbindAnalyzer(docTypeFieldId);
	}

	@Inject
	DocTypeFieldService _docTypeFieldService;
}
