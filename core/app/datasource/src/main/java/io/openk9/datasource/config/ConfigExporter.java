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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import io.openk9.datasource.config.model.ConfigEntity;
import io.openk9.datasource.config.model.ConfigEntityType;
import io.openk9.datasource.config.model.ConfigMetadata;
import io.openk9.datasource.config.model.ConfigPackage;
import io.openk9.datasource.config.model.representation.AclMappingRepresentation;
import io.openk9.datasource.config.model.representation.EnrichPipelineItemRepresentation;
import io.openk9.datasource.model.AclMapping;
import io.openk9.datasource.model.EnrichPipelineItem;
import io.openk9.datasource.model.EnrichPipelineItemKey;
import io.openk9.datasource.model.PluginDriverDocTypeFieldKey;
import io.openk9.datasource.model.TenantBinding;
import io.openk9.datasource.model.dto.base.K9EntityDTO;
import io.openk9.datasource.model.util.ExportIgnore;
import io.openk9.datasource.model.util.K9Entity;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import org.hibernate.reactive.mutiny.Mutiny;

/**
 * Export graph collector: walks the whole tenant configuration and turns it
 * into a portable {@link ConfigPackage}.
 * <p>
 * For every exportable type all instances are loaded and turned into a
 * {@link ConfigEntity}: {@code attributes} come from {@link ConfigEntityMapper},
 * {@code key} is the entity name (used for by-name matching on import) and
 * {@code references} carry the outgoing relationships as handles.
 * <p>
 * Only the <em>owning</em> side of each relationship is emitted, which keeps the
 * reference graph acyclic (the property {@link ConfigEntitySorter} relies on).
 * The set of edges is <em>derived</em> from the JPA model (see
 * {@link #deriveEdges()}): every owning association of an exportable entity that
 * points to another exportable type is followed, unless the field is annotated
 * {@link ExportIgnore}. Each edge is then collected with one flat
 * {@code select owner.id, target.id} query: the inner join skips unset foreign
 * keys and, unlike navigating a lazy to-one getter, never depends on the
 * association being fetched into the reactive session. Entities marked
 * {@link ExportIgnore} ({@code DataIndex}, {@code Scheduler},
 * {@code FileResource}, {@code Translation}, {@code TenantBinding}) are not
 * exportable, hence never followed. The {@code TenantBinding} pointers are
 * captured in the {@link ConfigMetadata} and secrets are redacted last.
 */
@ApplicationScoped
public class ConfigExporter {

	/**
	 * Every owning relationship between exportable entities, derived from the JPA
	 * model so a new association is followed automatically (and a removed one
	 * disappears). Each edge is collected with a single
	 * {@code select owner.id, target.id from Owner owner join owner.field target}
	 * query; a to-one relationship simply yields at most one row per owner.
	 */
	private static final List<EdgeSpec> EDGE_SPECS = deriveEdges();

	private final Mutiny.SessionFactory sessionFactory;
	private final ConfigEntityMapper mapper;
	private final ConfigRedactor redactor;

	// Cache of the reflectively-resolved ConfigEntityMapper.dto(entityClass) methods.
	private final Map<Class<?>, Method> dtoMethods = new ConcurrentHashMap<>();


	@Inject
	public ConfigExporter(
		Mutiny.SessionFactory sessionFactory,
		ConfigEntityMapper mapper,
		ConfigRedactor redactor) {

		this.sessionFactory = sessionFactory;
		this.mapper = mapper;
		this.redactor = redactor;
	}

	/**
	 * Exports the entire configuration of the given tenant as a portable,
	 * secret-free {@link ConfigPackage}.
	 *
	 * @param tenantId the schema/tenant to export
	 * @return the assembled package, with secrets already redacted
	 */
	public Uni<ConfigPackage> export(String tenantId) {
		return sessionFactory.withTransaction(tenantId, (s, t) -> doExport(s));
	}

	/**
	 * Node for the pluginDriver-to-docTypeField join, endpoints read from the key.
	 */
	private ConfigEntity aclMappingNode(AclMapping e) {
		PluginDriverDocTypeFieldKey key = e.getKey();

		Map<String, List<String>> refs = new LinkedHashMap<>();
		refs.put("pluginDriver", List.of(
			handle(ConfigEntityType.PLUGIN_DRIVER, key.getPluginDriverId())));
		refs.put("docTypeField", List.of(
			handle(ConfigEntityType.DOC_TYPE_FIELD, key.getDocTypeFieldId())));

		AclMappingRepresentation attributes = mapper.dto(e);
		String ref = handle(
			ConfigEntityType.ACL_MAPPING,
			key.getPluginDriverId(), key.getDocTypeFieldId());

		return new ConfigEntity(
			ref, ConfigEntityType.ACL_MAPPING, null, attributes, refs, null);
	}

	/**
	 * Loads every instance of the type and turns each into a node whose attributes
	 * come from the matching {@code ConfigEntityMapper.dto(...)} overload and whose
	 * references are read from the pre-built {@link EdgeIndex}.
	 */
	private Uni<List<ConfigEntity>> append(
		Uni<List<ConfigEntity>> acc, Mutiny.Session s, ConfigEntityType type,
		EdgeIndex edges) {

		return appendJoin(acc, s, type.getEntityType(), e -> {
			K9Entity entity = (K9Entity) e;
			return node(
				type, entity.getId(), toDto(type.getEntityType(), entity),
				edges.refs(type, entity.getId()));
		});
	}

	// --- entity collection (one sequential step per type on the session) -------

	/**
	 * Loads all instances of the type and turns each into a node via toNode,
	 * appending them to the accumulator.
	 */
	private <T> Uni<List<ConfigEntity>> appendJoin(
		Uni<List<ConfigEntity>> acc, Mutiny.Session s, Class<T> type,
		Function<T, ConfigEntity> toNode) {

		return acc.flatMap(list -> findAll(s, type).map(found -> {
			for (T entity : found) {
				list.add(toNode.apply(entity));
			}
			return list;
		}));
	}

	/**
	 * Wraps entities and metadata into a ConfigPackage, then redacts secrets last.
	 */
	private ConfigPackage assemble(
		List<ConfigEntity> entities, TenantBinding tenantBinding) {

		ConfigPackage configPackage = new ConfigPackage(
			ConfigPackage.CURRENT_SCHEMA_VERSION, metadata(tenantBinding), entities);

		redactor.redact(configPackage);

		return configPackage;
	}

	/**
	 * Runs every edge query sequentially on the session, accumulating the results
	 * into the EdgeIndex.
	 */
	private Uni<EdgeIndex> collectEdges(Mutiny.Session s) {
		Uni<EdgeIndex> acc = Uni.createFrom().item(new EdgeIndex());

		for (EdgeSpec spec : EDGE_SPECS) {
			acc = acc.flatMap(index -> s
				.createQuery(edgeCriteria(spec))
				.getResultList()
				.map(rows -> {
					for (Object[] row : rows) {
						index.add(
							spec, (Long) row[0],
							handle(spec.targetType(), (Long) row[1]));
					}
					return index;
				}));
		}

		return acc;
	}

	/**
	 * Loads every exportable type in turn and builds its nodes; the two
	 * composite-key join entities use dedicated builders.
	 */
	private Uni<List<ConfigEntity>> collectEntities(Mutiny.Session s, EdgeIndex edges) {
		Uni<List<ConfigEntity>> acc =
			Uni.createFrom().item(new ArrayList<ConfigEntity>());

		// Generic pass: every exportable type is loaded from ConfigEntityType, so a
		// new entity is picked up automatically once it is registered there.
		for (ConfigEntityType type : ConfigEntityType.values()) {
			if (isCompositeKeyEntity(type.getEntityType())) {
				continue;
			}
			acc = append(acc, s, type, edges);
		}

		// Composite-key join entities need dedicated builders (composite handle,
		// endpoints read from the embedded key), so they are collected explicitly.
		acc = appendJoin(acc, s, EnrichPipelineItem.class, this::enrichPipelineItemNode);
		acc = appendJoin(acc, s, AclMapping.class, this::aclMappingNode);

		return acc;
	}

	/**
	 * Orchestrates one transaction: collect edges, then nodes, then the
	 * TenantBinding, and assemble them into the package.
	 */
	private Uni<ConfigPackage> doExport(Mutiny.Session s) {
		return collectEdges(s)
			.flatMap(edges -> collectEntities(s, edges))
			.flatMap(entities -> s
				.find(TenantBinding.class, 1L)
				.map(tenantBinding -> assemble(entities, tenantBinding)));
	}

	/**
	 * The cached {@code ConfigEntityMapper.dto(entityClass)} method; fails loudly
	 * when a registered exportable type has no matching mapper overload.
	 */
	private Method dtoMethod(Class<?> entityClass) {
		return dtoMethods.computeIfAbsent(entityClass, clazz -> {
			try {
				return ConfigEntityMapper.class.getMethod("dto", clazz);
			}
			catch (NoSuchMethodException e) {
				throw new IllegalStateException(
					"ConfigEntityMapper has no dto(" + clazz.getSimpleName() + ")", e);
			}
		});
	}

	// --- join entities: composite handle, endpoints read from the embedded key -

	/**
	 * Query that collects the edges of one relationship. It is the Criteria
	 * equivalent of the JPQL:
	 *
	 * <pre>{@code
	 * select owner.id, target.id
	 * from <OwnerEntity> owner
	 * join owner.<relationship> target
	 * }</pre>
	 *
	 * For every owner that has the association set it returns the pair
	 * {@code (owner id, target id)} — the two foreign-key ids that form one edge.
	 * The join is an inner join, so owners whose association is null are skipped;
	 * only the ids are projected, no entity is materialised. This is why edges are
	 * read this way and not by navigating entities: the target is never re-loaded,
	 * and a lazy to-one (which would read {@code null} in the reactive session
	 * unless fetched) is avoided entirely.
	 */
	private CriteriaQuery<Object[]> edgeCriteria(EdgeSpec spec) {
		CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);

		Root<?> owner = query.from(spec.ownerType().getEntityType());
		Join<?, ?> target = owner.join(spec.relationship());
		query.multiselect(owner.get("id"), target.get("id"));

		return query;
	}

	/**
	 * Node for the pipeline-to-item join: composite handle plus both endpoints
	 * read straight from the embedded key.
	 */
	private ConfigEntity enrichPipelineItemNode(EnrichPipelineItem e) {
		EnrichPipelineItemKey key = e.getKey();

		Map<String, List<String>> refs = new LinkedHashMap<>();
		refs.put("enrichPipeline", List.of(
			handle(ConfigEntityType.ENRICH_PIPELINE, key.getEnrichPipelineId())));
		refs.put("enrichItem", List.of(
			handle(ConfigEntityType.ENRICH_ITEM, key.getEnrichItemId())));

		EnrichPipelineItemRepresentation attributes = mapper.dto(e);
		String ref = handle(
			ConfigEntityType.ENRICH_PIPELINE_ITEM,
			key.getEnrichPipelineId(), key.getEnrichItemId());

		return new ConfigEntity(
			ref, ConfigEntityType.ENRICH_PIPELINE_ITEM, null, attributes, refs, null);
	}

	// --- node / reference helpers ----------------------------------------------

	/**
	 * Loads all rows of the given entity type with a Criteria "select all" query.
	 */
	private <T> Uni<List<T>> findAll(Mutiny.Session s, Class<T> type) {
		CriteriaQuery<T> query = sessionFactory.getCriteriaBuilder().createQuery(type);
		query.from(type);
		return s.createQuery(query).getResultList();
	}

	/**
	 * Captures the tenant-wide pointers (virtual host, default bucket/embedding/LLM)
	 * from the TenantBinding into ConfigMetadata.
	 */
	private ConfigMetadata metadata(TenantBinding tenantBinding) {
		String exportedAt = OffsetDateTime.now().toString();

		if (tenantBinding == null) {
			return new ConfigMetadata(exportedAt, null, null, null, null);
		}

		return new ConfigMetadata(
			exportedAt,
			tenantBinding.getVirtualHost(),
			handleOrNull(ConfigEntityType.BUCKET, tenantBinding.getBucket()),
			handleOrNull(
				ConfigEntityType.EMBEDDING_MODEL, tenantBinding.getEmbeddingModel()),
			handleOrNull(
				ConfigEntityType.LARGE_LANGUAGE_MODEL,
				tenantBinding.getLargeLanguageModel())
		);
	}

	/**
	 * Builds a ConfigEntity: handle from type+id, key from the DTO name. Used only
	 * for the non-join types, whose attributes are always a {@link K9EntityDTO}.
	 */
	private ConfigEntity node(
		ConfigEntityType type, Long id, K9EntityDTO attributes,
		Map<String, List<String>> references) {

		return new ConfigEntity(
			handle(type, id), type, attributes.getName(), attributes, references, null);
	}

	// --- TenantBinding metadata ------------------------------------------------

	/**
	 * Maps an entity to its export DTO through the {@code dto(<EntityType>)}
	 * overload of {@link ConfigEntityMapper}, resolved reflectively and cached.
	 */
	private K9EntityDTO toDto(Class<?> entityClass, K9Entity entity) {
		try {
			return (K9EntityDTO) dtoMethod(entityClass).invoke(mapper, entity);
		}
		catch (ReflectiveOperationException e) {
			throw new IllegalStateException(
				"Cannot map " + entityClass.getSimpleName() + " to its export DTO", e);
		}
	}

	/**
	 * All association fields (to-one and to-many) on the entity's class hierarchy.
	 */
	private static List<Field> associationFields(Class<?> entity) {
		return associationFields(
			entity, ManyToOne.class, OneToOne.class, OneToMany.class, ManyToMany.class);
	}

	// --- edge derivation from the JPA model ------------------------------------

	/**
	 * Fields on the entity's class hierarchy carrying any of the given annotations.
	 */
	@SafeVarargs
	private static List<Field> associationFields(
		Class<?> entity, Class<? extends java.lang.annotation.Annotation>... markers) {

		List<Field> fields = new ArrayList<>();
		for (Class<?> c = entity; c != null && c != Object.class; c = c.getSuperclass()) {
			for (Field field : c.getDeclaredFields()) {
				for (var marker : markers) {
					if (field.isAnnotationPresent(marker)) {
						fields.add(field);
						break;
					}
				}
			}
		}
		return fields;
	}

	/**
	 * Builds the edge list by reflecting over the entity classes registered in
	 * {@link ConfigEntityType}: for each exportable entity (composite-key join
	 * entities excepted, they are handled by dedicated builders) every owning
	 * association whose target is itself exportable becomes an edge, unless the
	 * field is annotated {@link ExportIgnore}.
	 */
	private static List<EdgeSpec> deriveEdges() {
		Map<Class<?>, ConfigEntityType> typeByEntity = new HashMap<>();
		for (ConfigEntityType type : ConfigEntityType.values()) {
			typeByEntity.put(type.getEntityType(), type);
		}

		List<EdgeSpec> specs = new ArrayList<>();
		for (ConfigEntityType ownerType : ConfigEntityType.values()) {
			Class<?> owner = ownerType.getEntityType();
			if (isCompositeKeyEntity(owner)) {
				continue;
			}
			for (Field field : associationFields(owner)) {
				if (field.isAnnotationPresent(ExportIgnore.class)) {
					continue;
				}
				Class<?> target = owningTarget(field);
				ConfigEntityType targetType =
					target == null ? null : typeByEntity.get(target);
				if (targetType == null) {
					continue;
				}
				specs.add(new EdgeSpec(ownerType, field.getName(), targetType));
			}
		}
		return List.copyOf(specs);
	}

	/**
	 * Element type of a generic collection field (e.g. {@code Set<Tab>} yields
	 * {@code Tab}), or null.
	 */
	private static Class<?> elementType(Field field) {
		if (field.getGenericType() instanceof ParameterizedType parameterized) {
			Type argument = parameterized.getActualTypeArguments()[0];
			if (argument instanceof Class<?> clazz) {
				return clazz;
			}
		}
		return null;
	}

	/**
	 * Handle of a single-id entity, e.g. "BUCKET-42".
	 */
	private static String handle(ConfigEntityType type, Long id) {
		return type.name() + "-" + id;
	}

	/**
	 * Handle of a composite-key (join) entity, e.g. "ACL_MAPPING-7-13".
	 */
	private static String handle(ConfigEntityType type, Long firstId, Long secondId) {
		return type.name() + "-" + firstId + "-" + secondId;
	}

	/**
	 * Handle of the target, or null when the target is absent.
	 */
	private static String handleOrNull(ConfigEntityType type, K9Entity target) {
		return target == null ? null : handle(type, target.getId());
	}

	// --- edge collection -------------------------------------------------------

	/**
	 * True when the entity has a composite key ({@code @EmbeddedId}): a join entity.
	 */
	private static boolean isCompositeKeyEntity(Class<?> entity) {
		for (Field field : associationFields(entity, EmbeddedId.class)) {
			return true;
		}
		return false;
	}

	/**
	 * The owning association target of {@code field}, or {@code null} when the
	 * field is not an owning association. {@code @ManyToOne} is always owning;
	 * the other kinds own only when they declare no {@code mappedBy}.
	 */
	private static Class<?> owningTarget(Field field) {
		if (field.isAnnotationPresent(ManyToOne.class)) {
			return field.getType();
		}

		OneToOne oneToOne = field.getAnnotation(OneToOne.class);
		if (oneToOne != null && oneToOne.mappedBy().isEmpty()) {
			return field.getType();
		}

		OneToMany oneToMany = field.getAnnotation(OneToMany.class);
		if (oneToMany != null && oneToMany.mappedBy().isEmpty()) {
			return elementType(field);
		}

		ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
		if (manyToMany != null && manyToMany.mappedBy().isEmpty()) {
			return elementType(field);
		}

		return null;
	}

	private record EdgeSpec(
		ConfigEntityType ownerType, String relationship, ConfigEntityType targetType
	) {}

	/**
	 * Owner handle ↦ (relationship ↦ target handles), built from the edge
	 * queries. {@link #refs(ConfigEntityType, Long)} returns a fresh, mutable
	 * copy so callers cannot mutate the shared index.
	 */
	private static final class EdgeIndex {

		private final Map<ConfigEntityType, Map<Long, Map<String, List<String>>>>
			byOwner = new LinkedHashMap<>();

		/**
		 * Records one edge under owner -> relationship -> target handle.
		 */
		void add(EdgeSpec spec, Long ownerId, String targetHandle) {
			byOwner
				.computeIfAbsent(spec.ownerType(), k -> new LinkedHashMap<>())
				.computeIfAbsent(ownerId, k -> new LinkedHashMap<>())
				.computeIfAbsent(spec.relationship(), k -> new ArrayList<>())
				.add(targetHandle);
		}

		Map<String, List<String>> refs(ConfigEntityType ownerType, Long ownerId) {
			Map<String, List<String>> found = byOwner
				.getOrDefault(ownerType, Map.of())
				.get(ownerId);

			return found == null
				? new LinkedHashMap<>()
				: new LinkedHashMap<>(found);
		}

	}

}
