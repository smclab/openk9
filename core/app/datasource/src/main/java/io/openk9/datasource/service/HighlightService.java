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
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

@ApplicationScoped
public class HighlightService extends BaseK9EntityService<Highlight, HighlightDTO> {

	private static final Logger LOGGER = Logger.getLogger(HighlightService.class);

	HighlightService(HighlightMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public Class<Highlight> getEntityClass() {
		return Highlight.class;
	}

	@Override
	public Uni<Highlight> create(Highlight entity) {
		return sessionFactory.withTransaction(
			(session, transaction) -> create(session, entity)
		);
	}

	@Override
	public Uni<Highlight> create(HighlightDTO dto) {
		return sessionFactory.withTransaction(
				(session, transaction) -> createHighlightWithFields(session, dto)
					.flatMap(highlight ->
						create(session, highlight)
					)
			);
	}

	@Override
	public Uni<Highlight> update(long id, HighlightDTO dto) {
		return sessionFactory.withTransaction(
			(session, transaction) -> findById(session, id)
				.flatMap(highlight ->
					update(id, dto)
				)
		);
	}

	private Uni<Highlight> createHighlightWithFields(Mutiny.Session session, HighlightDTO dto) {
		Highlight highlight = mapper.create(dto);

		if (dto.getMatchedFieldIds() != null) {
			Multi.createFrom().items(dto.getMatchedFieldIds())
				.onItem()
				.transformToUniAndConcatenate(fieldId ->
					session.find(DocTypeField.class, fieldId)
				)
				.collect().asSet()
				.map(matchedFields -> {
					highlight.setMatchedFields(matchedFields);
					return highlight;
				});
		}

		return Multi.createFrom().iterable(dto.getFieldIds())
			.onItem()
			.transformToUniAndConcatenate(fieldId ->
				session.find(DocTypeField.class, fieldId)
			)
			.collect().asSet()
			.map(fields -> {
				highlight.setFields(fields);
				return highlight;
			});
	}
}
