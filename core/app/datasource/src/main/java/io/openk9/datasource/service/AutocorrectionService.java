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

import io.openk9.datasource.mapper.AutocorrectionMapper;
import io.openk9.datasource.model.Autocorrection;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.dto.base.AutocorrectionDTO;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
public class AutocorrectionService extends BaseK9EntityService<Autocorrection, AutocorrectionDTO>{

	@Inject
	DocTypeFieldService docTypeFieldService;

	AutocorrectionService(AutocorrectionMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public Class<Autocorrection> getEntityClass() {
		return Autocorrection.class;
	}

	@Override
	public Uni<Autocorrection> create(Autocorrection entity) {
		return sessionFactory.withTransaction(
			(session, transaction) -> create(session, entity)
		);
	}

	@Override
	public Uni<Autocorrection> create(AutocorrectionDTO dto) {
		if (dto.getAutocorrectionDocTypeFieldId() == null || dto.getAutocorrectionDocTypeFieldId() == 0L) {
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

	private Uni<Autocorrection> createTransient(Mutiny.Session session, AutocorrectionDTO dto) {
		Autocorrection transientAutocorrection = mapper.create(dto);

		transientAutocorrection.setAutocorrectionDocTypeField(
			session.getReference(DocTypeField.class, dto.getAutocorrectionDocTypeFieldId())
		);

		return Uni.createFrom().item(transientAutocorrection);
	}
}
