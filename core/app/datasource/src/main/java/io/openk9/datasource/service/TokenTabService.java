package io.openk9.datasource.service;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.mapper.TokenTabMapper;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.model.TokenTab_;
import io.openk9.datasource.model.dto.TokenTabDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

;

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
		return sessionFactory.withTransaction(
			s -> s.fetch(tokenTab.getDocTypeField()));
	}

	public Uni<DocTypeField> getDocTypeField(long tokenTabId) {
		return sessionFactory.withTransaction(s -> findById(tokenTabId)
			.flatMap(t -> s.fetch(t.getDocTypeField())));
	}

	public Uni<Set<TokenTab.ExtraParam>> getExtraParams(TokenTab tokenTab) {
		return sessionFactory
			.withTransaction((s, t) -> s
				.fetch(tokenTab.getExtraParams())
				.map(TokenTab::getExtraParamsSet)
			);
	}

	public Uni<Tuple2<TokenTab, DocTypeField>> bindDocTypeFieldToTokenTab(
		long tokenTabId, long docTypeFieldId) {
		return sessionFactory.withTransaction((s) -> findById(s, tokenTabId)
			.onItem()
			.ifNotNull()
			.transformToUni(tokenTab -> docTypeFieldService.findById(s, docTypeFieldId)
				.onItem()
				.ifNotNull()
				.transformToUni(docTypeField -> {
					tokenTab.setDocTypeField(docTypeField);
					return persist(s, tokenTab)
						.map(newTokenTab -> Tuple2.of(newTokenTab, docTypeField));
				})
			)
		);
	}

	public Uni<Tuple2<TokenTab, DocTypeField>> unbindDocTypeFieldFromTokenTab(
		long tokenTabId, long docTypeFieldId) {
		return sessionFactory.withTransaction((s) -> findById(s, tokenTabId)
			.onItem()
			.ifNotNull()
			.transformToUni(tokenTab -> docTypeFieldService.findById(s, docTypeFieldId)
				.onItem()
				.ifNotNull()
				.transformToUni(docTypeField -> {
					tokenTab.setDocTypeField(null);
					return persist(s, tokenTab)
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
	DocTypeFieldService docTypeFieldService;

	public Uni<TokenTab> addExtraParam(long id, String key, String value) {
		return getSessionFactory()
			.withTransaction(s ->
				findById(s, id)
					.flatMap(TokenTabService::fetchExtraParams)
					.flatMap(tokenTab -> {
						tokenTab.addExtraParam(key, value);
						return persist(s, tokenTab);
					})
			);
	}

	public Uni<TokenTab> removeExtraParam(int id, String key) {
		return getSessionFactory()
			.withTransaction(s ->
				findById(s, id)
					.flatMap(TokenTabService::fetchExtraParams)
					.flatMap(tokenTab -> {
						tokenTab.removeExtraParam(key);
						return persist(s, tokenTab);
					})
			);	}

	private static Uni<TokenTab> fetchExtraParams(TokenTab tokenTab) {
		return Mutiny
			.fetch(tokenTab.getExtraParams())
			.flatMap(extraParams -> {
				tokenTab.setExtraParams(extraParams);
				return Uni.createFrom().item(tokenTab);
			});
	}
}
