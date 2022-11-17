package io.openk9.datasource.service;

import io.openk9.datasource.graphql.util.relay.Connection;
import io.openk9.datasource.mapper.TokenTabMapper;
import io.openk9.datasource.model.Annotator_;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.model.TokenTab_;
import io.openk9.datasource.model.dto.TokenTabDTO;
import io.openk9.datasource.model.util.Mutiny2;
import io.openk9.datasource.resource.util.SortBy;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

@ApplicationScoped
public class TokenTabService extends BaseK9EntityService<TokenTab, TokenTabDTO> {
	TokenTabService(TokenTabMapper mapper) {this.mapper = mapper;}

	@Override
	public Class<TokenTab> getEntityClass(){
		return TokenTab.class;
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {TokenTab_.NAME, TokenTab_.TOKEN_TYPE};
	}

	public Uni<DocTypeField> getDocTypeField(TokenTab tokenTab) {
		return withTransaction(
			s -> Mutiny2.fetch(s, tokenTab.getDocTypeField()));
	}

	public Uni<DocTypeField> getDocTypeField(long tokenTabId) {
		return withTransaction(
			() -> findById(tokenTabId).flatMap(this::getDocTypeField));
	}

	public Uni<Tuple2<TokenTab, DocTypeField>> bindDocTypeFieldToTokenTab(
		long tokenTabId, long docTypeFieldId) {
		return withTransaction((s) -> findById(tokenTabId)
			.onItem()
			.ifNotNull()
			.transformToUni(tokenTab -> docTypeFieldService.findById(docTypeFieldId)
				.onItem()
				.ifNotNull()
				.transformToUni(docTypeField -> {
					tokenTab.setDocTypeField(docTypeField);
					return persist(tokenTab)
						.map(newTokenTab -> Tuple2.of(newTokenTab, docTypeField));
				})));
	}

	public Uni<Tuple2<TokenTab, DocTypeField>> unbindDocTypeFieldFromTokenTab(
		long tokenTabId, long docTypeFieldId) {
		return withTransaction((s) -> findById(tokenTabId)
			.onItem()
			.ifNotNull()
			.transformToUni(tokenTab -> docTypeFieldService.findById(docTypeFieldId)
				.onItem()
				.ifNotNull()
				.transformToUni(docTypeField -> {
					tokenTab.setDocTypeField(null);
					return persist(tokenTab)
						.map(newTokenTab -> Tuple2.of(newTokenTab, docTypeField));
				})));
	}


	public Uni<Connection<DocTypeField>> getDocTypeFieldsNotInTokenTab(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList) {
		return findJoinConnection(
			id, TokenTab_.DOC_TYPE_FIELD, DocTypeField.class,
			docTypeFieldService.getSearchFields(), after, before, first, last,
			searchText, sortByList, true);
	}

	@Inject
	private DocTypeFieldService docTypeFieldService;

}
