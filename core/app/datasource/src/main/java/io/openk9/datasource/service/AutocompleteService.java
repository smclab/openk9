/*
 * Copyright (C) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
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

import java.util.HashSet;
import java.util.Objects;
import jakarta.enterprise.context.ApplicationScoped;

import io.openk9.datasource.mapper.AutocompleteMapper;
import io.openk9.datasource.model.Autocomplete;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.dto.base.AutocompleteDTO;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
public class AutocompleteService extends BaseK9EntityService<Autocomplete, AutocompleteDTO>{

	AutocompleteService(AutocompleteMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public Uni<Autocomplete> create(Autocomplete entity) {
		return sessionFactory.withTransaction(
			(session, transaction) -> create(session, entity)
		);
	}

	@Override
	public Uni<Autocomplete> create(AutocompleteDTO dto) {
		if (dto.getFieldIds() == null || dto.getFieldIds().isEmpty()) {
			return super.create(dto);
		}
		else {
			return sessionFactory.withTransaction(
				(session, transaction) ->
					createTransient(session, dto)
						.flatMap(autocorrection ->
							create(session, autocorrection)
						)
			);
		}
	}

	public Uni<Autocomplete> patch(long autocompleteId, AutocompleteDTO dto) {
		return sessionFactory.withTransaction(
			(session, transaction) -> findById(session, autocompleteId)
				.call(autocomplete -> Mutiny.fetch(autocomplete.getFields()))
				.flatMap(autocomplete -> {
					var newStateAutocomplete = mapper.patch(autocomplete, dto);
					var fieldIds = dto.getFieldIds();

					if (fieldIds != null) {
						// Ripulisci tutti i field esistenti
						newStateAutocomplete.getFields().clear();

						// Se ci sono nuovi field da aggiungere
						if (!fieldIds.isEmpty()) {
							return Multi.createFrom().iterable(fieldIds)
								.onItem().transformToUniAndConcatenate(
									fieldId -> session.find(DocTypeField.class, fieldId)
								)
								.select().where(Objects::nonNull)
								.collect().asList()
								.map(fields -> {
									newStateAutocomplete.setFields(new HashSet<>(fields));
									return newStateAutocomplete;
								});
						}
					}

					return Uni.createFrom().item(newStateAutocomplete);
				})
		);
	}

	public Uni<Autocomplete> update(long autocompleteId, AutocompleteDTO dto) {
		return sessionFactory.withTransaction(
			(session, transaction) -> findById(session, autocompleteId)
				.call(autocomplete -> Mutiny.fetch(autocomplete.getFields()))
				.flatMap(autocomplete -> {
					var newStateAutocomplete = mapper.update(autocomplete, dto);
					var fieldIds = dto.getFieldIds();

					// Ripulisci tutti i field esistenti
					newStateAutocomplete.getFields().clear();

					if (fieldIds != null) {
						// Se ci sono nuovi field da aggiungere
						if (!fieldIds.isEmpty()) {
							return Multi.createFrom().iterable(fieldIds)
								.onItem().transformToUniAndConcatenate(
									fieldId -> session.find(DocTypeField.class, fieldId)
								)
								.select().where(Objects::nonNull)
								.collect().asList()
								.map(fields -> {
									newStateAutocomplete.setFields(new HashSet<>(fields));
									return newStateAutocomplete;
								});
						}
					}

					return Uni.createFrom().item(newStateAutocomplete);
				})
		);
	}

	@Override
	public Class<Autocomplete> getEntityClass() {
		return Autocomplete.class;
	}

	private Uni<Autocomplete> createTransient(Mutiny.Session session, AutocompleteDTO dto) {
		var transientAutocomplete = mapper.create(dto);

		return Multi.createFrom().iterable(dto.getFieldIds())
			.onItem()
			.transformToUniAndConcatenate(fieldId ->
				session.find(DocTypeField.class, fieldId)
			)
			.collect().asSet()
			.map(fields -> {
				transientAutocomplete.setFields(fields);
				return transientAutocomplete;
			});
	}
}
