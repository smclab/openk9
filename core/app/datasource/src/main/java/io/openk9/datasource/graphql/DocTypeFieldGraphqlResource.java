package io.openk9.datasource.graphql;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.datasource.model.Analyzer;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.dto.DocTypeFieldDTO;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.service.DocTypeFieldService;
import io.openk9.datasource.service.util.Tuple2;
import io.openk9.datasource.validation.FieldValidator;
import io.openk9.datasource.validation.Response;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Set;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class DocTypeFieldGraphqlResource {

	@Query
	public Uni<Connection<DocTypeField>> getDocTypeFields(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return docTypeFieldService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	public Uni<DocTypeField> parent(
		@Source DocTypeField docTypeField) {
		return docTypeFieldService.getParent(docTypeField);
	}

	public Uni<Connection<DocTypeField>> subFields(
		@Source DocTypeField docTypeField,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@Description("if notEqual is true, it returns unbound entities") @DefaultValue("false") boolean notEqual) {

		return docTypeFieldService.getSubDocTypeFields(
			docTypeField, after, before, first, last, searchText, sortByList,
			notEqual);
	}


	public Uni<Analyzer> analyzer(@Source DocTypeField docTypeField) {
		return docTypeFieldService.getAnalyzer(docTypeField.getId());
	}

	@Mutation
	public Uni<Response<DocTypeField>> createSubField(
		@Id long parentDocTypeFieldId, DocTypeFieldDTO docTypeFieldDTO) {

		return Uni.createFrom().deferred(() -> {

			List<FieldValidator> validatorList =
				docTypeFieldService.getValidator().validate(docTypeFieldDTO);

			if (validatorList.isEmpty()) {

				return docTypeFieldService
					.createSubField(parentDocTypeFieldId, docTypeFieldDTO)
					.onItemOrFailure()
					.transform((e, t) -> {

						List<FieldValidator> newValidatorList = null;

						if (t != null) {
							newValidatorList = List.of(
								FieldValidator.of("fieldName", t.getMessage()));
						}

						return Response.of(e, newValidatorList);

					});
			}

			return Uni.createFrom().item(Response.of(null, validatorList));
		});

	}

	@Mutation
	public Uni<Tuple2<DocTypeField, Analyzer>> bindAnalyzerToDocTypeField(
		@Id long docTypeFieldId, @Id long analyzerId) {
		return docTypeFieldService.bindAnalyzer(docTypeFieldId, analyzerId);
	}

	@Mutation
	public Uni<Tuple2<DocTypeField, Analyzer>> unbindAnalyzerFromDocTypeField(
		@Id long docTypeFieldId) {
		return docTypeFieldService.unbindAnalyzer(docTypeFieldId);
	}

	@Inject
	DocTypeFieldService docTypeFieldService;
}
