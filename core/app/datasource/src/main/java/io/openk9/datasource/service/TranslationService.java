package io.openk9.datasource.service;

import io.openk9.datasource.model.Translation;
import io.openk9.datasource.model.TranslationKey;
import io.openk9.datasource.model.dto.TranslationDTO;
import io.openk9.datasource.model.util.K9Entity;
import io.openk9.datasource.model.util.LocalizedEntity;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.common.Identifier;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
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

		return em.withStatelessTransaction(session -> session
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

	public <T extends K9Entity> Uni<Map<Long, Map<String, String>>> getTranslationMaps(
		Class<T> entityClass, List<Long> ids) {

		String className = entityClass.getName();

		return em.withStatelessTransaction(session -> session
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

		return em.withTransaction((session, transaction) -> session
			.find(Translation.class, Identifier.id("pk", pkValue))
			.chain(entity -> {
				if (entity != null) {
					entity.setValue(value);
					return session.persist(entity);
				}
				else {
					Translation translation = new Translation();
					translation.setPk(pkValue);
					translation.setValue(value);
					return session.persist(translation);
				}
			}));
	}

	public <T extends K9Entity> Uni<Void> deleteTranslation(
		Class<T> entityClass, Long id, String language, String key) {

		TranslationKey pkValue = new TranslationKey(language, entityClass.getName(), id, key);

		return em.withTransaction((session, transaction) -> session
			.find(Translation.class, Identifier.id("pk", pkValue))
			.chain(entity -> {
				if (entity != null) {
					return session.remove(entity);
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
