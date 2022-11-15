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

import io.openk9.datasource.model.DataIndex_;
import io.openk9.datasource.model.Datasource_;
import io.openk9.datasource.model.DocType;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.DocTypeField_;
import io.openk9.datasource.model.DocType_;
import io.openk9.datasource.model.FieldType;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.TenantBinding;
import io.openk9.datasource.model.TenantBinding_;
import io.openk9.datasource.model.Bucket_;
import io.openk9.datasource.sql.TransactionInvoker;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import java.util.ArrayList;
import java.util.List;

@Path("/v1/date-filter")
public class DateFilterResource {

	@Context
	HttpServerRequest request;

	@GET
	public Uni<List<DocTypeFieldResponseDto>> getFields() {
		return getDocTypeFieldList(request.host());
	}

	private Uni<List<DocTypeFieldResponseDto>> getDocTypeFieldList(String virtualhost) {
		return transactionInvoker.withTransaction(session -> {

			CriteriaBuilder cb = transactionInvoker.getCriteriaBuilder();

			CriteriaQuery<DocTypeField> query = cb.createQuery(DocTypeField.class);

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

			query.select(fetch);

			query.where(
				cb.equal(
					tenantBindingFetch.get(
						TenantBinding_.virtualHost),
					virtualhost
				)
			);

			return session.createQuery(query).getResultList().map(docTypeFields -> {

				List<DocTypeFieldResponseDto> docTypeFieldResponseDtos = new ArrayList<>();

				for(DocTypeField docTypeField : docTypeFields){
					DocTypeFieldResponseDto docTypeFieldResponseDto = new DocTypeFieldResponseDto();
					docTypeFieldResponseDto.setId(docTypeField.getId());
					docTypeFieldResponseDto.setField(docTypeField.getName());
					docTypeFieldResponseDto.setLabel(docTypeField.getFieldName());

					docTypeFieldResponseDtos.add(docTypeFieldResponseDto);
				}
				return docTypeFieldResponseDtos;

			});
			});
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class DocTypeFieldResponseDto {
		private String field;
		private Long id;
		private String label;
	}

	@Inject
	TransactionInvoker transactionInvoker;
}
