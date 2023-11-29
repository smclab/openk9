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

package io.openk9.datasource.web;

import io.openk9.datasource.mapper.BucketResourceMapper;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.Bucket_;
import io.openk9.datasource.model.DataIndex_;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.DocTypeField_;
import io.openk9.datasource.model.DocType_;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.TenantBinding;
import io.openk9.datasource.model.TenantBinding_;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.service.TranslationService;
import io.openk9.datasource.web.dto.DocTypeFieldResponseDTO;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.inject.Inject;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import java.util.List;
import java.util.stream.Stream;

@Path("/v1/date-filter")
public class DateFilterResource {

	@Context
	HttpServerRequest request;

	@GET
	public Uni<List<DocTypeFieldResponseDTO>> getFields(
		@QueryParam("translated") @DefaultValue("false") boolean translated
	) {
		return getDocTypeFieldList(request.host(), translated);
	}

	private Uni<List<DocTypeFieldResponseDTO>> getDocTypeFieldList(String virtualhost, boolean translated) {
		return sessionFactory.withTransaction(session -> {

			CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();

			CriteriaQuery<Tuple> query = cb.createTupleQuery();

			Root<Bucket> from = query.from(Bucket.class);

			Join<Bucket, TenantBinding> tenantBindingFetch =
				from.join(Bucket_.tenantBinding);

			Join<DocType, DocTypeField> fetch =
				from.join(Bucket_.datasources)
					.join(Datasource_.dataIndex)
					.join(DataIndex_.docTypes)
					.join(DocType_.docTypeFields);

			fetch.on(
				cb.and(
					cb.isTrue(fetch.get(DocTypeField_.SEARCHABLE)),
					cb.equal(fetch.get(DocTypeField_.fieldType), FieldType.DATE)
				));

			SetJoin<DocTypeField, DocTypeField> subDocTypeFieldJoin =
				fetch.join(DocTypeField_.subDocTypeFields, JoinType.LEFT);

			subDocTypeFieldJoin.on(
				cb.and(
					cb.isTrue(subDocTypeFieldJoin.get(DocTypeField_.SEARCHABLE)),
					cb.equal(subDocTypeFieldJoin.get(DocTypeField_.fieldType), FieldType.DATE)
				));

			query.multiselect(fetch, subDocTypeFieldJoin);

			query.where(
				cb.equal(
					tenantBindingFetch.get(
						TenantBinding_.virtualHost),
					virtualhost
				)
			);

			return session
				.createQuery(query)
				.getResultList()
				.map(tList ->
					tList
						.stream()
						.flatMap(t -> {

							Stream.Builder<DocTypeField> builder =
								Stream.builder();

							DocTypeField docTypeField1 =
								t.get(0, DocTypeField.class);

							if (docTypeField1 != null) {
								builder.add(docTypeField1);
							}

							DocTypeField docTypeField2 =
								t.get(1, DocTypeField.class);

							if (docTypeField2 != null) {
								builder.add(docTypeField2);
							}

							return builder.build();
						})
				)
				.chain(docTypeFields -> {
					List<DocTypeField> docTypeFieldList = docTypeFields.toList();
					if (translated) {
						return translationService
							.getTranslationMaps(
								DocTypeField.class,
								docTypeFieldList.stream()
									.map(K9Entity::getId)
									.toList())
							.map(maps -> mapper.toDocTypeFieldResponseDtoList(docTypeFieldList, maps));
					}
					else {
						return Uni
							.createFrom()
							.item(mapper.toDocTypeFieldResponseDtoList(docTypeFieldList));
					}
				});
		});
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	public static class DateFilterResponseDto {
		private String field;
		private Long id;
		private String label;
	}

	@Inject
	Mutiny.SessionFactory sessionFactory;

	@Inject
	TranslationService translationService;

	@Inject
	BucketResourceMapper mapper;

}
