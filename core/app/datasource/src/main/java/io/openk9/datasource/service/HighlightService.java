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

import io.openk9.datasource.mapper.HighlightMapper;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.Highlight;
import io.openk9.datasource.model.dto.base.HighlightDTO;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Set;

@ApplicationScoped
public class HighlightService extends BaseK9EntityService<Highlight, HighlightDTO> {

	@Inject
	DocTypeFieldService docTypeFieldService;

	HighlightService(HighlightMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public Class<Highlight> getEntityClass() {
		return Highlight.class;
	}

	@Override
	public Uni<Highlight> create(HighlightDTO dto) {
		return sessionFactory.withTransaction(
			(session, transaction) ->
				createTransient(dto)
					.flatMap(highlight ->
						create(session, highlight)
					)
		);
	}

	@Override
	public Uni<Highlight> update(long id, HighlightDTO dto) {
		return sessionFactory.withTransaction(
			(session, transaction) ->
				findById(session, id)
					.flatMap(highlight -> {
						var newStateHighlight = mapper.update(highlight, dto);

						var docTypeFieldIds = docTypeFieldService.findByIds(dto.getFieldIds());

						return docTypeFieldIds.flatMap(docTypeFields -> {
							newStateHighlight.setFields(Set.copyOf(docTypeFields));

							if (dto.getMatchedFieldIds() != null && !dto.getMatchedFieldIds().isEmpty()) {
								var matchedDocTypeFieldIds = docTypeFieldService.findByIds(dto.getMatchedFieldIds());

								return matchedDocTypeFieldIds.flatMap(matchedDocTypeFields -> {
									newStateHighlight.setMatchedFields(Set.copyOf(matchedDocTypeFields));
									return session.merge(newStateHighlight);
								});
							}

							return session.merge(newStateHighlight);
						});
					})
		);
	}

	@Override
	public Uni<Highlight> patch(long id, HighlightDTO dto) {
		return sessionFactory.withTransaction(
			(session, transaction) ->
				findById(session, id)
					.flatMap(highlight -> {
						var newStateHighlight = mapper.patch(highlight, dto);

						if (!dto.getFieldIds().isEmpty() || !dto.getMatchedFieldIds().isEmpty()) {

							var docTypeFieldIds = docTypeFieldService.findByIds(dto.getFieldIds());
							var matchedDocTypeFieldIds = docTypeFieldService.findByIds(dto.getMatchedFieldIds());

							return Uni.combine().all().unis(docTypeFieldIds, matchedDocTypeFieldIds)
								.asTuple()
								.flatMap(tuple -> {
									Set<DocTypeField> fields = Set.copyOf(tuple.getItem1());
									Set<DocTypeField> matchedFields = Set.copyOf(tuple.getItem2());

									newStateHighlight.setFields(fields);
									newStateHighlight.setMatchedFields(matchedFields);

									return session.merge(newStateHighlight);
								});
						}

						return session.merge(newStateHighlight);
					})
		);
	}

	private Uni<Highlight> createTransient(HighlightDTO dto) {
		var transientHighlight = mapper.create(dto);

		var docTypeFieldIds = docTypeFieldService.findByIds(dto.getFieldIds());

		return docTypeFieldIds.flatMap(docTypeFields -> {
			transientHighlight.setFields(Set.copyOf(docTypeFields));

			if (dto.getMatchedFieldIds() != null && !dto.getMatchedFieldIds().isEmpty()) {
				var matchedDocTypeFieldIds = docTypeFieldService.findByIds(dto.getMatchedFieldIds());

				return matchedDocTypeFieldIds.flatMap(matchedDocTypeFields -> {
					transientHighlight.setMatchedFields(Set.copyOf(matchedDocTypeFields));
					return Uni.createFrom().item(transientHighlight);
				});
			}

			return Uni.createFrom().item(transientHighlight);
		});
	}
}
