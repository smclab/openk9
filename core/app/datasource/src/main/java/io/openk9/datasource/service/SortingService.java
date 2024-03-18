package io.openk9.datasource.service;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.mapper.SortingMapper;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.Sorting;
import io.openk9.datasource.model.Sorting_;
import io.openk9.datasource.model.dto.SortingDTO;
import io.openk9.datasource.model.dto.TranslationDTO;
import io.openk9.datasource.model.dto.TranslationKeyDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.openk9.datasource.service.util.Tuple2;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Set;

;

@ApplicationScoped
public class SortingService extends BaseK9EntityService<Sorting, SortingDTO> {
	SortingService(SortingMapper mapper) {this.mapper = mapper;}

	@Override
	public Class<Sorting> getEntityClass(){
		return Sorting.class;
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {Sorting_.NAME, Sorting_.DESCRIPTION};
	}

	public Uni<DocTypeField> getDocTypeField(Sorting sorting) {
		return sessionFactory.withTransaction(s -> s
			.merge(sorting)
			.flatMap(merged -> s.fetch(merged.getDocTypeField()))
		);
	}

	public Uni<DocTypeField> getDocTypeField(long sortingId) {
		return sessionFactory.withTransaction(s -> findById(sortingId)
			.flatMap(t -> s.fetch(t.getDocTypeField())));
	}

	public Uni<Tuple2<Sorting, DocTypeField>> bindDocTypeFieldToSorting(
		long sortingId, long docTypeFieldId) {
		return sessionFactory.withTransaction((s) -> findById(s, sortingId)
			.onItem()
			.ifNotNull()
			.transformToUni(sorting -> docTypeFieldService.findById(s, docTypeFieldId)
				.onItem()
				.ifNotNull()
				.transformToUni(docTypeField -> {
					sorting.setDocTypeField(docTypeField);
					return persist(s, sorting)
						.map(newSorting -> Tuple2.of(newSorting, docTypeField));
				})
			)
		);
	}

	public Uni<Tuple2<Sorting, DocTypeField>> unbindDocTypeFieldFromSorting(
		long sortingId, long docTypeFieldId) {
		return sessionFactory.withTransaction((s) -> findById(s, sortingId)
			.onItem()
			.ifNotNull()
			.transformToUni(sorting -> docTypeFieldService.findById(s, docTypeFieldId)
				.onItem()
				.ifNotNull()
				.transformToUni(docTypeField -> {
					sorting.setDocTypeField(null);
					return persist(s, sorting)
						.map(newSorting -> Tuple2.of(newSorting, docTypeField));
				})));
	}


	public Uni<Connection<DocTypeField>> getDocTypeFieldsNotInSorting(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList) {
		return findJoinConnection(
			id, Sorting_.DOC_TYPE_FIELD, DocTypeField.class,
			docTypeFieldService.getSearchFields(), after, before, first, last,
			searchText, sortByList, true);
	}

	public Uni<Void> addTranslation(Long id, TranslationDTO dto) {
		return translationService.addTranslation(
			Sorting.class, id, dto.getLanguage(), dto.getKey(), dto.getValue());
	}

	public Uni<Void> deleteTranslation(Long id, TranslationKeyDTO dto) {
		return translationService.deleteTranslation(
			Sorting.class, id, dto.getLanguage(), dto.getKey());
	}

	@Inject
	DocTypeFieldService docTypeFieldService;

	@Inject
	TranslationService translationService;

}
