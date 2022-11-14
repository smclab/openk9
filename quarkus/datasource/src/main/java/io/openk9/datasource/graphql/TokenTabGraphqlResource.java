package io.openk9.datasource.graphql;

import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.service.TokenTabService;
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
public class TokenTabGraphqlResource {

	public Uni<DocTypeField> docTypeField(@Source TokenTab tokenTab) {
		return tokenTabService.getDocTypeField(tokenTab.getId());
	}

	@Mutation
	public Uni<Tuple2<TokenTab, DocTypeField>> bindDocTypeFieldToTokenTab(
		@Id long tokenTabId, @Id long docTypeFieldId) {
		return tokenTabService.bindDocTypeFieldToTokenTab(tokenTabId, docTypeFieldId);
	}

	@Mutation
	public Uni<Tuple2<TokenTab, DocTypeField>> unbindDocTypeFieldFromTokenTab(
		@Id long id, @Id long docTypeFieldId) {
		return tokenTabService.unbindDocTypeFieldFromTokenTab(id, docTypeFieldId);
	}

	@Inject
	TokenTabService tokenTabService;
}
