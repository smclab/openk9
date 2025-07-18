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

package io.openk9.datasource.service;

import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.common.graphql.util.relay.Connection;
import io.openk9.common.util.SortBy;
import io.openk9.datasource.mapper.TokenTabMapper;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.TokenTab;
import io.openk9.datasource.model.TokenTab_;
import io.openk9.datasource.model.dto.base.TokenTabDTO;
import io.openk9.datasource.model.dto.request.TokenTabWithDocTypeFieldDTO;
import io.openk9.datasource.service.util.Tuple2;

import io.smallrye.mutiny.Uni;

;

@ApplicationScoped
public class TokenTabService extends BaseK9EntityService<TokenTab, TokenTabDTO> {
	TokenTabService(TokenTabMapper mapper) {this.mapper = mapper;}

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

	@Override
	public Uni<TokenTab> create(TokenTabDTO dto) {
		if ( dto instanceof TokenTabWithDocTypeFieldDTO withDocTypeFieldDTO) {
			var transientTokenTab = mapper.create(withDocTypeFieldDTO);

			return sessionFactory.withTransaction(
				(s, transaction) -> super.create(s, transientTokenTab)
					.flatMap(tokenTab -> {

						var docTypeFieldId = withDocTypeFieldDTO.getDocTypeFieldId();

						if (docTypeFieldId != null) {
							var docTypeField =
								s.getReference(DocTypeField.class, docTypeFieldId);

							tokenTab.setDocTypeField(docTypeField);
						}

						return s.persist(tokenTab)
							.flatMap(__ -> s.merge(tokenTab));
					})
			);
		}
		return super.create(dto);
	}

	public Uni<DocTypeField> getDocTypeField(long tokenTabId) {
		return sessionFactory.withTransaction(s -> findById(tokenTabId)
			.flatMap(t -> s.fetch(t.getDocTypeField())));
	}

	public Uni<DocTypeField> getDocTypeField(TokenTab tokenTab) {
		return sessionFactory.withTransaction(s -> s
			.merge(tokenTab)
			.flatMap(merged -> s.fetch(merged.getDocTypeField()))
		);
	}

	public Uni<Connection<DocTypeField>> getDocTypeFieldsNotInTokenTab(
		Long id, String after, String before, Integer first, Integer last,
		String searchText, Set<SortBy> sortByList) {
		return findJoinConnection(
			id, TokenTab_.DOC_TYPE_FIELD, DocTypeField.class,
			docTypeFieldService.getSearchFields(), after, before, first, last,
			searchText, sortByList, true);
	}

	@Override
	public Class<TokenTab> getEntityClass(){
		return TokenTab.class;
	}

	@Override
	public String[] getSearchFields() {
		return new String[] {TokenTab_.NAME, TokenTab_.TOKEN_TYPE};
	}

	@Override
	public Uni<TokenTab> patch(long id, TokenTabDTO dto) {
		if (dto instanceof TokenTabWithDocTypeFieldDTO withDocTypeFieldDTO) {

			return sessionFactory.withTransaction(
				(s, transaction) -> findById(s, id)
					.flatMap(tokenTab -> {

						var newStateTokenTab =
							mapper.patch(tokenTab, withDocTypeFieldDTO);
						var docTypeFieldId = withDocTypeFieldDTO.getDocTypeFieldId();

						if (docTypeFieldId != null) {
							var docTypeField =
								s.getReference(DocTypeField.class, docTypeFieldId);

							newStateTokenTab.setDocTypeField(docTypeField);
						}

						return s.merge(newStateTokenTab)
							.map(__ -> newStateTokenTab);
					})
			);
		}
		return super.patch(id, dto);
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

	@Override
	public Uni<TokenTab> update(long id, TokenTabDTO dto) {
		if (dto instanceof TokenTabWithDocTypeFieldDTO withDocTypeFieldDTO) {

			return sessionFactory.withTransaction(
				(s, transaction) -> findById(s, id)
					.flatMap(tokenTab -> {

						var newStateTokenTab =
							mapper.update(tokenTab, withDocTypeFieldDTO);
						var docTypeFieldId = withDocTypeFieldDTO.getDocTypeFieldId();

						DocTypeField docTypeField = null;

						if (docTypeFieldId != null) {
							docTypeField =
								s.getReference(DocTypeField.class, docTypeFieldId);
						}

						newStateTokenTab.setDocTypeField(docTypeField);

						return s.merge(newStateTokenTab)
							.map(__ -> newStateTokenTab);
					})
			);
		}
		return super.update(id, dto);
	}

	@Inject
	DocTypeFieldService docTypeFieldService;
}
