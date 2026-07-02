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

package io.openk9.datasource.config;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.openk9.datasource.config.model.ConfigEntity;
import io.openk9.datasource.config.model.ConfigEntityType;
import io.openk9.datasource.config.model.ConfigPackage;
import io.openk9.datasource.config.model.ImportMode;
import io.openk9.datasource.config.model.ImportPlan;
import io.openk9.datasource.config.model.PlannedAction;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.reactive.mutiny.Mutiny;

/**
 * Import matcher: given a {@link ConfigPackage}, decides for every entity
 * whether it already exists in the target tenant and, from that, the action the
 * importer will take ({@code CREATE}, {@code OVERWRITE} or {@code SKIP}). It
 * produces an {@link ImportPlan} and performs no writes; the transactional
 * apply, the handle-to-id rewiring and the {@code TenantBinding} rebind belong
 * to a later step.
 * <p>
 * Matching is generic, mirroring the exporter: the natural (business) identity
 * of each entity is <em>derived from the JPA model</em> (see
 * {@link #naturalKeyOf(Class)}) rather than switched on per type. A scalar
 * component (e.g. {@code name}) is matched by value read from the typed
 * attributes DTO; an association component (e.g. {@code docType},
 * {@code searchConfig}) is matched through the ancestor already resolved in this
 * package — the same handle/reference model the exporter emits.
 * <p>
 * Entities are processed in {@link ConfigEntitySorter} order so ancestors are
 * resolved first. Composite-key join entities ({@code EnrichPipelineItem},
 * {@code AclMapping}) are not matched here: the importer rebuilds them from
 * their endpoints, so they carry no action in the plan.
 */
@ApplicationScoped
public class ConfigMatcher {

	private final Mutiny.SessionFactory sessionFactory;
	private final ConfigEntitySorter sorter;

	// Cache of the reflectively-derived natural key of each entity class.
	private final Map<Class<?>, List<KeyComponent>> naturalKeys =
		new ConcurrentHashMap<>();

	@Inject
	public ConfigMatcher(
		Mutiny.SessionFactory sessionFactory, ConfigEntitySorter sorter) {

		this.sessionFactory = sessionFactory;
		this.sorter = sorter;
	}

	/**
	 * Matches the package against the given tenant and returns the import plan.
	 *
	 * @param tenantId the target schema/tenant
	 * @param pkg      the configuration package to import
	 * @param mode     what to do with entities that already exist
	 * @return the ordered plan of actions (no writes performed)
	 */
	public Uni<ImportPlan> plan(
		String tenantId, ConfigPackage pkg, ImportMode mode) {

		return sessionFactory.withTransaction(tenantId, (s, t) -> doPlan(s, pkg, mode));
	}

	/**
	 * Walks the entities in dependency-first order, matching each against the
	 * tenant on the same session and folding the results into the plan; ancestor
	 * ids resolved along the way feed the matching of their dependents.
	 */
	private Uni<ImportPlan> doPlan(
		Mutiny.Session s, ConfigPackage pkg, ImportMode mode) {

		List<ConfigEntity> ordered = sorter.sort(pkg.getEntities());
		Map<String, Long> matchedIds = new HashMap<>();

		Uni<List<PlannedAction>> acc =
			Uni.createFrom().item(new ArrayList<PlannedAction>());

		for (ConfigEntity entity : ordered) {
			if (isJoinEntity(entity.getType().getEntityType())) {
				continue;
			}
			acc = acc.flatMap(actions -> matchExisting(s, entity, matchedIds)
				.map(existingId -> {
					PlannedAction.Action action;
					if (existingId == null) {
						action = PlannedAction.Action.CREATE;
					}
					else {
						matchedIds.put(entity.getRef(), existingId);
						action = mode == ImportMode.OVERWRITE
							? PlannedAction.Action.OVERWRITE
							: PlannedAction.Action.SKIP;
					}
					actions.add(new PlannedAction(
						entity.getRef(), entity.getType(), action, existingId));
					return actions;
				}));
		}

		return acc.map(ImportPlan::new);
	}

	/**
	 * Finds the id of the existing entity that matches this package entity by its
	 * natural key, or {@code null} when none exists (a {@code CREATE}). Returns
	 * {@code null} early when an identity-composing ancestor is itself going to be
	 * created, since the entity cannot then match anything existing.
	 */
	private Uni<Long> matchExisting(
		Mutiny.Session s, ConfigEntity entity, Map<String, Long> matchedIds) {

		Class<?> entityClass = entity.getType().getEntityType();

		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<?> root = query.from(entityClass);
		query.select(root.get("id"));

		List<Predicate> predicates = new ArrayList<>();
		for (KeyComponent component : naturalKeyOf(entityClass)) {
			String fieldName = component.field().getName();

			if (component.association()) {
				String handle = referenceHandle(entity, fieldName);
				if (handle == null) {
					// No such reference: the foreign key was null at export.
					predicates.add(cb.isNull(root.get(fieldName)));
				}
				else {
					Long ancestorId = matchedIds.get(handle);
					if (ancestorId == null) {
						// The ancestor will be created, so no existing row can match.
						return Uni.createFrom().nullItem();
					}
					predicates.add(cb.equal(root.get(fieldName).get("id"), ancestorId));
				}
			}
			else {
				Object value = readProperty(entity.getAttributes(), fieldName);
				Class<?> fieldType = component.field().getType();
				if (value == null) {
					predicates.add(cb.isNull(root.get(fieldName)));
				}
				else if (fieldType.isEnum() && value instanceof String text) {
					// The DTO carries the enum as its name; the entity field is an
					// enum, so match the constant exactly (no case folding).
					predicates.add(cb.equal(root.get(fieldName), toEnum(fieldType, text)));
				}
				else if (value instanceof String text) {
					// Uniform case-insensitive match, as the base findByName does.
					predicates.add(cb.equal(
						cb.lower(root.<String>get(fieldName)), text.toLowerCase()));
				}
				else {
					predicates.add(cb.equal(root.get(fieldName), value));
				}
			}
		}

		query.where(predicates.toArray(new Predicate[0]));

		return s.createQuery(query)
			.setMaxResults(1)
			.getResultList()
			.map(ids -> ids.isEmpty() ? null : ids.get(0));
	}

	/**
	 * The single target handle of a to-one reference, or {@code null} when the
	 * package carries no such reference for the entity.
	 */
	private static String referenceHandle(ConfigEntity entity, String fieldName) {
		Map<String, List<String>> references = entity.getReferences();
		if (references == null) {
			return null;
		}
		List<String> handles = references.get(fieldName);
		return handles == null || handles.isEmpty() ? null : handles.get(0);
	}

	// --- natural-key derivation from the JPA model -----------------------------

	/**
	 * The natural key of the entity class as an ordered list of components,
	 * derived from the JPA model and cached. Precedence: a {@code @Table} unique
	 * constraint &gt; {@code @Column(unique = true)} fields.
	 *
	 * @throws IllegalStateException when no identity can be derived
	 */
	public List<KeyComponent> naturalKeyOf(Class<?> entityClass) {
		return naturalKeys.computeIfAbsent(entityClass, ConfigMatcher::deriveNaturalKey);
	}

	private static List<KeyComponent> deriveNaturalKey(Class<?> entityClass) {
		// 1. @Table unique constraint: resolve each column back to its field.
		Table table = entityClass.getAnnotation(Table.class);
		if (table != null && table.uniqueConstraints().length > 0) {
			Map<String, Field> byColumn = columnToField(entityClass);
			List<KeyComponent> components = new ArrayList<>();
			for (String columnName : table.uniqueConstraints()[0].columnNames()) {
				Field field = byColumn.get(columnName);
				if (field == null) {
					throw new IllegalStateException(
						"Unique-constraint column '" + columnName + "' of "
							+ entityClass.getSimpleName()
							+ " maps to no field with @Column/@JoinColumn(name)");
				}
				components.add(component(field));
			}
			return List.copyOf(components);
		}

		// 2. @Column(unique = true) fields (typically the single "name").
		List<KeyComponent> components = new ArrayList<>();
		for (Field field : allFields(entityClass)) {
			Column column = field.getAnnotation(Column.class);
			if (column != null && column.unique()) {
				components.add(component(field));
			}
		}
		if (!components.isEmpty()) {
			return List.copyOf(components);
		}

		throw new IllegalStateException(
			"No derivable natural key for " + entityClass.getName()
				+ ": declare it with a @Table unique constraint or "
				+ "a @Column(unique = true) field");
	}

	/**
	 * A key component over the field: an association when it is an owning to-one
	 * ({@code @ManyToOne}/{@code @OneToOne}), otherwise a scalar column.
	 */
	private static KeyComponent component(Field field) {
		boolean association = field.isAnnotationPresent(ManyToOne.class)
			|| field.isAnnotationPresent(OneToOne.class);
		return new KeyComponent(field, association);
	}

	/**
	 * Maps each mapped column name ({@code @Column(name)}/{@code @JoinColumn(name)})
	 * to its field, across the entity's class hierarchy.
	 */
	private static Map<String, Field> columnToField(Class<?> entityClass) {
		Map<String, Field> byColumn = new LinkedHashMap<>();
		for (Field field : allFields(entityClass)) {
			Column column = field.getAnnotation(Column.class);
			if (column != null && !column.name().isEmpty()) {
				byColumn.putIfAbsent(column.name(), field);
			}
			JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
			if (joinColumn != null && !joinColumn.name().isEmpty()) {
				byColumn.putIfAbsent(joinColumn.name(), field);
			}
		}
		return byColumn;
	}

	/**
	 * All declared fields on the entity's class hierarchy (excluding Object).
	 */
	private static List<Field> allFields(Class<?> entityClass) {
		List<Field> fields = new ArrayList<>();
		for (Class<?> c = entityClass;
			 c != null && c != Object.class;
			 c = c.getSuperclass()) {

			for (Field field : c.getDeclaredFields()) {
				fields.add(field);
			}
		}
		return fields;
	}

	/**
	 * True when the entity has a composite key ({@code @EmbeddedId}): a join
	 * entity, not matched by this component.
	 */
	public static boolean isJoinEntity(Class<?> entityClass) {
		for (Class<?> c = entityClass;
			 c != null && c != Object.class;
			 c = c.getSuperclass()) {

			for (Field field : c.getDeclaredFields()) {
				if (field.isAnnotationPresent(EmbeddedId.class)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * The enum constant of the given enum type with the given name.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Object toEnum(Class<?> enumType, String name) {
		return Enum.valueOf((Class<? extends Enum>) enumType, name);
	}

	/**
	 * Reads a bean property from the typed attributes DTO by its getter.
	 */
	private static Object readProperty(Object bean, String property) {
		String getter = "get"
			+ Character.toUpperCase(property.charAt(0)) + property.substring(1);
		try {
			Method method = bean.getClass().getMethod(getter);
			return method.invoke(bean);
		}
		catch (ReflectiveOperationException e) {
			throw new IllegalStateException(
				"Cannot read '" + property + "' from "
					+ bean.getClass().getSimpleName(), e);
		}
	}

	/**
	 * One field composing a natural key: {@code association} tells matching to
	 * resolve it through a reference handle rather than a scalar value.
	 */
	public record KeyComponent(Field field, boolean association) {}

}
