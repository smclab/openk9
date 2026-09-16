/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.datasource.graphql;

import java.util.List;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.ValidationException;

import io.openk9.common.graphql.SortBy;
import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.web.FieldValidator;
import io.openk9.common.util.web.Response;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.DocTypeTemplate;
import io.openk9.datasource.model.dto.base.DocTypeDTO;
import io.openk9.datasource.model.dto.base.DocTypeFieldDTO;
import io.openk9.datasource.model.dto.request.DocTypeFieldWithAnalyzerDTO;
import io.openk9.datasource.model.dto.request.DocTypeWithTemplateDTO;
import io.openk9.datasource.service.DataIndexService;
import io.openk9.datasource.service.DocTypeFieldService;
import io.openk9.datasource.service.DocTypeService;
import io.openk9.datasource.service.exception.K9Error;
import io.openk9.datasource.service.util.K9EntityEvent;
import io.openk9.datasource.service.util.Tuple2;

import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;
import org.hibernate.reactive.mutiny.Mutiny;

@GraphQLApi
@ApplicationScoped
@CircuitBreaker
public class DocTypeGraphqlResource {

	@Inject
	DataIndexService dataIndexService;
	@Inject
	DocTypeFieldService docTypeFieldService;
	@Inject
	DocTypeService docTypeService;
	@Inject
	Mutiny.SessionFactory sessionFactory;

	/**
	 * @see DataIndexService#alignDataIndexes(long, boolean)
	 */
	@Mutation
	@Description("""
		Aligns to the model every index that uses this docType, writing the
		settings and the mappings its docTypes derive and regenerating its index
		template. Only the index each datasource is currently pointing at is
		touched: the ones a reindex left behind are neither searched nor
		written.
		One index failing does not stop the others, so an outcome is returned
		for each of them.
		An index is not closed unless closeIfNeeded says so: when one has to be,
		the outcome is CLOSE_REQUIRED and nothing was written to the live index,
		so the caller can ask and call again. While an index is closed it is
		neither searchable nor writable.
		An APPLIED outcome means OpenSearch accepted what was sent to it, not
		that the index matches the model: a mapping is additive, so what the
		model no longer declares stays in the index.
		The Boost, Searchable, Exclude and Sortable properties of a docTypeField
		need no alignment: they never reach the index and take effect as soon as
		they are saved.
		""")
	public Uni<List<DataIndexService.IndexAlignment>> alignIndexes(
		@Id long docTypeId,
		@Description("whether an index may be closed to take the definitions "
			+ "the model declares")
		@DefaultValue("false") boolean closeIfNeeded) {

		return dataIndexService.alignDataIndexes(docTypeId, closeIfNeeded)
			.onFailure(ValidationException.class)
			.transform(K9Error::new);
	}

	@Mutation
	public Uni<Tuple2<DocType, DocTypeTemplate>> bindDocTypeToDocTypeTemplate(
		@Id @Name("docTypeId") long docTypeId,
		@Id @Name("docTypeTemplateId") long docTypeTemplateId) {
		return docTypeService.setDocTypeTemplate(docTypeId, docTypeTemplateId);
	}

	public Uni<Response<DocType>> createDocType(DocTypeDTO docTypeDTO) {
		return docTypeService.getValidator().create(docTypeDTO);
	}

	@Mutation
	@Description("""
		Deletes a DocType entity by its ID after validating the provided name matches the entity.
		Requires both the docTypeId and docTypeName (as a confirmation mechanism) to prevent
		accidental deletions.
		""")
	public Uni<DocType> deleteDocType(@Id long docTypeId, String docTypeName) {
		return docTypeService.deleteById(docTypeId, docTypeName);
	}

	public Uni<DocType> docType(@Source DocTypeField docTypeField) {
		return sessionFactory.withTransaction(s -> s
			.merge(docTypeField)
			.flatMap(merged -> s.fetch(merged.getDocType()))
		);
	}

	@Mutation
	public Uni<Response<DocType>> docType(
		@Id Long id, DocTypeDTO docTypeDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createDocType(docTypeDTO);
		} else {
			return patch
				? patchDocType(id, docTypeDTO)
				: updateDocType(id, docTypeDTO);
		}

	}

	@Subscription
	public Multi<DocType> docTypeCreated() {
		return docTypeService
			.getProcessor()
			.filter(K9EntityEvent::isCreate)
			.map(K9EntityEvent::getEntity);
	}

	@Subscription
	public Multi<DocType> docTypeDeleted() {
		return docTypeService
			.getProcessor()
			.filter(K9EntityEvent::isDelete)
			.map(K9EntityEvent::getEntity);
	}

	@Mutation
	@Description("""
		Creates or updates a DocTypeField entity under the specified DocType.
		If docTypeFieldId is null, a new DocTypeField is created.
		Otherwise, updates or patches the existing DocTypeField depending on the patch flag.
		Returns validation errors if the provided DTO fails validation.
		Requires docTypeFieldName (as a confirmation mechanism) to prevent accidental modifications
		when updating or patching an existing DocTypeField.
		""")
	public Uni<Response<DocTypeField>> docTypeField(
		@Id long docTypeId, @Id Long docTypeFieldId, DocTypeFieldDTO docTypeFieldDTO,
		@DefaultValue("false") boolean patch, String docTypeFieldName) {

		return Uni.createFrom().deferred(() -> {

			List<FieldValidator> validatorList =
				docTypeFieldService.getValidator().validate(docTypeFieldDTO);

			if (validatorList.isEmpty()) {

				Uni<Response<DocTypeField>> result;

				if (docTypeFieldId == null) {
					result = docTypeService.addDocTypeField(docTypeId, docTypeFieldDTO)
						.map(e -> Response.of(e.right, null));
				} else if (patch) {
					result = docTypeFieldService.patch(
							docTypeFieldId, docTypeFieldDTO, docTypeFieldName)
						.map(e -> Response.of(e, null));
				} else {
					result = docTypeFieldService.update(
							docTypeFieldId, docTypeFieldDTO, docTypeFieldName)
						.map(e -> Response.of(e, null));
				}

				return result.onItemOrFailure().transform(
					(response, t) -> {
					if (t != null) {
						return Response.of(
							null, List.of(FieldValidator.of("error", t.getMessage())));
					}
					return response;
				});
			}

			return Uni.createFrom().item(Response.of(null, validatorList));
		});

	}

	@Mutation
	@Description("""
		Creates or updates a DocTypeField entity with an associated Analyzer.
		If docTypeFieldId is null, a new DocTypeField is created under the specified DocType.
		Otherwise, updates or patches the existing DocTypeField depending on the patch flag.
		Requires docTypeFieldName (as a confirmation mechanism) to prevent accidental modifications
		when updating or patching an existing DocTypeField.
		""")
	public Uni<Response<DocTypeField>> docTypeFieldWithAnalyzer(
		@Id long docTypeId, @Id Long docTypeFieldId,
		DocTypeFieldWithAnalyzerDTO docTypeFieldWithAnalyzerDTO,
		@DefaultValue("false") boolean patch, String docTypeFieldName) {

		return docTypeField(
			docTypeId, docTypeFieldId, docTypeFieldWithAnalyzerDTO, patch, docTypeFieldName);
	}

	public Uni<Connection<DocTypeField>> docTypeFields(
		@Source DocType docType,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@Description("if notEqual is true, it returns unbound entities") @DefaultValue("false") boolean notEqual) {
		return getDocTypeFieldsFromDocType(
			docType.getId(), after, before, first, last, searchText, sortByList,
			notEqual);
	}

	public Uni<DocTypeTemplate> docTypeTemplate(@Source DocType docType) {
		return sessionFactory.withTransaction(s -> s
			.merge(docType)
			.flatMap(merged -> s.fetch(merged.getDocTypeTemplate()))
		);
	}

	@Subscription
	public Multi<DocType> docTypeUpdated() {
		return docTypeService
			.getProcessor()
			.filter(K9EntityEvent::isUpdate)
			.map(K9EntityEvent::getEntity);
	}

	@Mutation
	public Uni<Response<DocType>> docTypeWithTemplate(
		@Id Long id, DocTypeWithTemplateDTO docTypeWithTemplateDTO,
		@DefaultValue("false") boolean patch) {

		if (id == null) {
			return createDocType(docTypeWithTemplateDTO);
		}
		else {
			return patch
				? patchDocType(id, docTypeWithTemplateDTO)
				: updateDocType(id, docTypeWithTemplateDTO);
		}

	}

	@Query
	public Uni<DocType> getDocType(@Id long id) {
		return docTypeService.findById(id);
	}

	@Query
	public Uni<DocTypeField> getDocTypeField(@Id long id) {
		return docTypeFieldService.findById(id);
	}

	@Query
	public Uni<Connection<DocTypeField>> getDocTypeFieldsFromDocType(
		@Id long docTypeId,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@Description("if notEqual is true, it returns unbound entities") @DefaultValue("false") boolean notEqual) {
		return docTypeService.getDocTypeFieldsConnection(
			docTypeId, after, before, first, last, searchText, sortByList, notEqual);
	}

	@Query
	public Uni<Connection<DocTypeField>> getDocTypeFieldsFromDocTypeByParent(
		@Id long docTypeId,
		@Description("id of the parent docTypeField (0 if root )") long parentId,
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList,
		@Description("if notEqual is true, it returns unbound entities") @DefaultValue("false") boolean notEqual) {
		return docTypeService.getDocTypeFieldsConnectionByParent(
			docTypeId, parentId, after, before, first, last, searchText, sortByList, notEqual);
	}

	@Query
	public Uni<Connection<DocType>> getDocTypes(
		@Description("fetching only nodes after this node (exclusive)") String after,
		@Description("fetching only nodes before this node (exclusive)") String before,
		@Description("fetching only the first certain number of nodes") Integer first,
		@Description("fetching only the last certain number of nodes") Integer last,
		String searchText, Set<SortBy> sortByList) {
		return docTypeService.findConnection(
			after, before, first, last, searchText, sortByList);
	}

	public Uni<Response<DocType>> patchDocType(@Id long id, DocTypeDTO docTypeDTO) {
		return docTypeService.getValidator().patch(id, docTypeDTO);
	}

	@Mutation
	public Uni<Tuple2<DocType, Long>> removeDocTypeField(
		@Id long docTypeId, @Id long docTypeFieldId) {
		return docTypeService.removeDocTypeField(docTypeId, docTypeFieldId);
	}

	@Mutation
	public Uni<DocType> unbindDocTypeTemplateFromDocType(
		@Id @Name("docTypeId") long docTypeId) {
		return docTypeService.unsetDocType(docTypeId);
	}

	public Uni<Response<DocType>> updateDocType(@Id long id, DocTypeDTO docTypeDTO) {
		return docTypeService.getValidator().update(id, docTypeDTO);
	}

}