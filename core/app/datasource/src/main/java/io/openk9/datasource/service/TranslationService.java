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

import io.openk9.datasource.model.Translation;
import io.openk9.datasource.model.TranslationKey;
import io.openk9.datasource.model.dto.TranslationDTO;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.model.util.LocalizedEntity;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.hibernate.reactive.common.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class TranslationService extends BaseK9EntityService<Translation, TranslationDTO> {

	public  <T extends K9Entity, S extends LocalizedEntity<T>>  Uni<S> getLocalizedEntity(
		Class<T> entityClass,
		T entity,
		BiFunction<? super T, Map<String, String>, ? extends S> localizedEntityFactory) {

		return this
			.getTranslationMap(
				entityClass,
				entity.getId()
			)
			.map(translationMap -> localizedEntityFactory
				.apply(entity, translationMap)
			);
	}

	public  <T extends K9Entity, S extends LocalizedEntity<T>>  Uni<List<S>> getLocalizedEntities(
		Class<T> entityClass,
		List<T> entities,
		BiFunction<? super T, Map<String, String>, ? extends S> localizedEntityFactory) {

		return this
			.getTranslationMaps(
				entityClass,
				entities
					.stream()
					.map(K9Entity::getId)
					.collect(Collectors.toList())
			)
			.map(translationMaps -> entities
				.stream()
				.map(entity -> localizedEntityFactory
					.apply(entity, translationMaps.get(entity.getId())))
				.collect(Collectors.toList())
			);
	}

	public <T extends K9Entity> Uni<Map<String, String>> getTranslationMap(
		Class<T> entityClass, Long id) {

		String className = entityClass.getName();

		return sessionFactory.withStatelessTransaction(session -> session
			.createQuery(
				"select t " +
				"from io.openk9.datasource.model.Translation t " +
				"where t.pk.className = :className " +
				"and t.pk.classPK = :classPk",
				Translation.class)
			.setParameter("className", className)
			.setParameter("classPk", id)
			.getResultList()
			.map(translations -> translations
				.stream()
				.collect(Collectors
					.toMap(
						t -> t.getPk().toString(),
						Translation::getValue)
				)
			)
		);
	}

	public <T extends K9Entity> Uni<Set<TranslationDTO>> getTranslationDTOs(
		Class<T> entityClass, Long id) {

		return getTranslationMap(entityClass, id)
			.map(translationMap -> translationMap
				.entrySet()
				.stream()
				.map(entry -> {
					String[] keys = entry.getKey().split("\\.");
					return TranslationDTO.builder()
						.language(keys[1])
						.key(keys[0])
						.value(entry.getValue())
						.build();
				})
				.collect(Collectors.toSet())
			);
	}

	public <T extends K9Entity> Uni<Map<Long, Map<String, String>>> getTranslationMaps(
		Class<T> entityClass, List<Long> ids) {

		String className = entityClass.getName();

		return sessionFactory.withStatelessTransaction(session -> session
			.createQuery(
				"select t " +
					"from io.openk9.datasource.model.Translation t " +
					"where t.pk.className = :className " +
					"and t.pk.classPK in (:classPks)",
				Translation.class)
			.setParameter("className", className)
			.setParameter("classPks", ids)
			.getResultList()
			.map(translations -> translations
				.stream()
				.collect(Collectors
					.toMap(
						Translation::getClassPK,
						t -> Map.of(t.getPk().toString(), t.getValue()),
						(m1, m2) -> Map.ofEntries(Stream
							.concat(m1.entrySet().stream(), m2.entrySet().stream())
							.<Map.Entry<String, String>>toArray(Map.Entry[]::new)
						)
					)
				)
			)
		);
	}

	public <T extends K9Entity> Uni<Void> addTranslation(
		Class<T> entityClass, Long id, String language, String key, String value) {

		TranslationKey pkValue = new TranslationKey(language, entityClass.getName(), id, key);

		return sessionFactory.withTransaction((session, transaction) -> session
			.find(Translation.class, Identifier.id("pk", pkValue))
			.chain(entity -> {
				if (entity != null) {
					entity.setValue(value);
					return persist(session, entity);
				}
				else {
					Translation translation = new Translation();
					translation.setPk(pkValue);
					translation.setValue(value);
					return persist(session, translation);
				}
			})
			.replaceWithVoid()
		);
	}

	public <T extends K9Entity> Uni<Void> deleteTranslation(
		Class<T> entityClass, Long id, String language, String key) {

		TranslationKey pkValue = new TranslationKey(language, entityClass.getName(), id, key);

		return sessionFactory.withTransaction((session, transaction) -> session
			.find(Translation.class, Identifier.id("pk", pkValue))
			.chain(entity -> {
				if (entity != null) {
					return remove(session, entity);
				}
				else {
					return Uni.createFrom().voidItem();
				}
			})
		);
	}

	@Override
	public Class<Translation> getEntityClass() {
		return Translation.class;
	}

}
