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
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.LinkedHashSet;
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
					.call(highlight -> Mutiny.fetch(highlight.getFields()))
					.call(highlight -> Mutiny.fetch(highlight.getMatchedFields()))
					.flatMap(highlight -> {
						var newStateHighlight = mapper.update(highlight, dto);
						return updateHighlight(dto, session, newStateHighlight);
					})
		);
	}

	@Override
	public Uni<Highlight> patch(long id, HighlightDTO dto) {
		return sessionFactory.withTransaction(
			(session, transaction) ->
				findById(session, id)
					.call(highlight -> Mutiny.fetch(highlight.getFields()))
					.call(highlight -> Mutiny.fetch(highlight.getMatchedFields()))
					.flatMap(highlight -> {
							var newStateHighlight = mapper.patch(highlight, dto);
							return patchHighlight(dto, session, newStateHighlight);
						}
					));
	}

	private Uni<Highlight> createTransient(HighlightDTO dto) {
		var transientHighlight = mapper.create(dto);

		var fieldsUniList = docTypeFieldService.findByIds(dto.getFieldIds());

		return fieldsUniList.flatMap(fieldsList -> {
			transientHighlight.setFields(new LinkedHashSet<>(fieldsList));

			if (dto.getMatchedFieldIds() != null) {
				var matchedFieldsUniList = docTypeFieldService.findByIds(dto.getMatchedFieldIds());

				return matchedFieldsUniList.flatMap(matchedFieldsList -> {
					transientHighlight.setMatchedFields(new LinkedHashSet<>(matchedFieldsList));
					return Uni.createFrom().item(transientHighlight);
				});
			}

			return Uni.createFrom().item(transientHighlight);
		});
	}

	private Uni<Highlight> updateHighlight(HighlightDTO dto, Mutiny.Session session, Highlight newStateHighlight) {
		var fieldsUniList = docTypeFieldService.findByIds(dto.getFieldIds());

		return fieldsUniList.flatMap(fieldsList -> {
			newStateHighlight.setFields(new LinkedHashSet<>(fieldsList));

			if (dto.getMatchedFieldIds() != null) {
				var matchedFieldsUniList = docTypeFieldService.findByIds(dto.getMatchedFieldIds());

				return matchedFieldsUniList.flatMap(matchedFieldsList -> {
					newStateHighlight.setMatchedFields(new LinkedHashSet<>(matchedFieldsList));
					return session.merge(newStateHighlight);
				});
			} else
				newStateHighlight.setMatchedFields(null);

			return session.merge(newStateHighlight);
		});
	}

	private Uni<Highlight> patchHighlight(HighlightDTO dto, Mutiny.Session session, Highlight newStateHighlight) {
		var fieldsUniList = docTypeFieldService.findByIds(dto.getFieldIds());

		return fieldsUniList.flatMap(fieldsList -> {
			newStateHighlight.setFields(new LinkedHashSet<>(fieldsList));

			if (dto.getMatchedFieldIds() != null) {
				var matchedFieldsUniList = docTypeFieldService.findByIds(dto.getMatchedFieldIds());

				return matchedFieldsUniList.flatMap(matchedFieldsList -> {
					newStateHighlight.setMatchedFields(new LinkedHashSet<>(matchedFieldsList));
					return session.merge(newStateHighlight);
				});
			}

			return session.merge(newStateHighlight);
		});
	}

	public Uni<Set<DocTypeField>> getFields(long  id) {
		return sessionFactory.withTransaction((session, transaction) ->
			findById(session, id)
				.flatMap(highlight ->
					Mutiny.fetch(highlight.getFields())
				)
		);
	}

	public Uni<Set<DocTypeField>> getMatchedFields(long id) {
		return sessionFactory.withTransaction((session, transaction) ->
			findById(session, id)
				.flatMap(highlight ->
					Mutiny.fetch(highlight.getMatchedFields())
				)
		);
	}
}
