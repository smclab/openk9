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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.openk9.datasource.config.model.ConfigEntity;
import io.openk9.datasource.config.model.ConfigMetadata;
import io.openk9.datasource.config.model.ConfigPackage;
import io.openk9.datasource.config.model.ImportMode;
import io.openk9.datasource.config.model.ImportPlan;
import io.openk9.datasource.config.model.ImportResult;
import io.openk9.datasource.config.model.PlannedAction;
import io.openk9.datasource.config.model.representation.AclMappingRepresentation;
import io.openk9.datasource.config.model.representation.EnrichPipelineItemRepresentation;
import io.openk9.datasource.model.AclMapping;
import io.openk9.datasource.model.Bucket;
import io.openk9.datasource.model.DocTypeField;
import io.openk9.datasource.model.EmbeddingModel;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.EnrichPipeline;
import io.openk9.datasource.model.EnrichPipelineItem;
import io.openk9.datasource.model.EnrichPipelineItemKey;
import io.openk9.datasource.model.LargeLanguageModel;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.PluginDriverDocTypeFieldKey;
import io.openk9.datasource.model.TenantBinding;
import io.openk9.datasource.model.util.K9Entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

/**
 * Applies an {@link ImportPlan} to a target tenant, in a single transaction.
 * <p>
 * The plan (from {@link ConfigMatcher}) is matched and applied on the same
 * session, so nothing changes between planning and writing. Regular entities are
 * created or overwritten generically: scalars come from the typed attributes DTO
 * through {@link ConfigEntityMapper}, whose {@code entity}/{@code update} overload is
 * selected by entity type and maps the DTO its own signature declares (so a
 * create-only field such as {@code RAGConfiguration.type} is mapped on create), and
 * every association is (re)wired from the package {@code references} to the real
 * target ids resolved along the way. The two composite-key join entities ({@code EnrichPipelineItem},
 * {@code AclMapping}), which carry no plan action, are rebuilt from their resolved
 * endpoints. Finally the tenant's {@code TenantBinding} is rebound to the imported
 * bucket and models.
 * <p>
 * Secrets are never clobbered: the exporter replaces them with a placeholder and
 * records their paths, so on overwrite the target's current value is restored into
 * the incoming DTO, and on create the redacted paths are dropped (left unset).
 */
@ApplicationScoped
public class ConfigImporter {

	private static final Logger log = Logger.getLogger(ConfigImporter.class);

	private final Mutiny.SessionFactory sessionFactory;
	private final ConfigMatcher matcher;
	private final ConfigEntityMapper configEntityMapper;
	private final ObjectMapper objectMapper;

	// Caches of the reflectively-resolved ConfigEntityMapper import methods,
	// keyed by entity type.
	private final Map<Class<?>, Method> entityMethods = new ConcurrentHashMap<>();
	private final Map<Class<?>, Method> updateMethods = new ConcurrentHashMap<>();

	@Inject
	public ConfigImporter(
		Mutiny.SessionFactory sessionFactory,
		ConfigMatcher matcher,
		ConfigEntityMapper configEntityMapper,
		ObjectMapper objectMapper) {

		this.sessionFactory = sessionFactory;
		this.matcher = matcher;
		this.configEntityMapper = configEntityMapper;
		this.objectMapper = objectMapper;
	}

	/**
	 * Matches the package against the tenant and applies the resulting plan.
	 *
	 * @param tenantId the target schema/tenant
	 * @param pkg      the configuration package to import
	 * @param mode     what to do with entities that already exist
	 * @return the summary of the applied plan
	 */
	public Uni<ImportResult> apply(
		String tenantId, ConfigPackage pkg, ImportMode mode) {

		return sessionFactory.withTransaction(tenantId, (s, t) ->
			matcher.plan(s, pkg, mode).flatMap(plan -> doApply(s, pkg, plan)));
	}

	private Uni<Void> applyAction(
		Mutiny.Session s, ConfigEntity entity, PlannedAction action,
		Map<String, Long> resolvedIds) {

		Class<?> entityClass = action.type().getEntityType();
		String ref = action.ref();

		return switch (action.action()) {
			case SKIP -> {
				resolvedIds.put(ref, action.existingId());
				yield Uni.createFrom().voidItem();
			}
			case CREATE -> {
				Object dto = stripRedacted(
					entity.getAttributes(), entity.getRedactedFields());
				K9Entity created = toEntity(entityClass, dto);
				yield wireAssociations(
						s, created, entity.getReferences(), resolvedIds, true)
					.flatMap(ignore -> s.persist(created).call(s::flush))
					.invoke(() -> resolvedIds.put(ref, created.getId()));
			}
			case OVERWRITE -> s.find(entityClass, action.existingId()).flatMap(found -> {
				K9Entity target = (K9Entity) found;
				Object dto = restoreRedacted(
					entity.getAttributes(), entity.getRedactedFields(), target);
				updateEntity(entityClass, target, dto);
				return wireAssociations(
						s, target, entity.getReferences(), resolvedIds, false)
					.flatMap(ignore -> s.persist(target).call(s::flush))
					.invoke(() -> resolvedIds.put(ref, action.existingId()));
			});
		};
	}

	private Object bindBack(ObjectNode tree, Class<?> attributesType) {
		try {
			return objectMapper.treeToValue(tree, attributesType);
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException(
				"Unable to rebind attributes to " + attributesType.getSimpleName(), e);
		}
	}

	private Uni<ImportResult> doApply(
		Mutiny.Session s, ConfigPackage pkg, ImportPlan plan) {

		Map<String, ConfigEntity> byRef = new HashMap<>();
		for (ConfigEntity entity : pkg.getEntities()) {
			byRef.put(entity.getRef(), entity);
		}
		Map<String, Long> resolvedIds = new HashMap<>();

		Uni<Void> chain = Uni.createFrom().voidItem();
		for (PlannedAction action : plan.getActions()) {
			ConfigEntity entity = byRef.get(action.ref());
			chain = chain.flatMap(
				ignore -> applyAction(s, entity, action, resolvedIds));
		}

		return chain
			.flatMap(ignore -> rebuildJoins(s, pkg, resolvedIds))
			.flatMap(joins -> rebindTenantBinding(s, pkg.getMetadata(), resolvedIds)
				.replaceWith(joins))
			.map(joins -> new ImportResult(
				(int) plan.count(PlannedAction.Action.CREATE) + joins.created(),
				(int) plan.count(PlannedAction.Action.OVERWRITE) + joins.overwritten(),
				(int) plan.count(PlannedAction.Action.SKIP) + joins.skipped(),
				resolvedIds));
	}

	/**
	 * The cached {@code ConfigEntityMapper.entity(...)} overload for the entity type,
	 * resolved by its return type. Resolution is by entity type, not by DTO: the
	 * overload declares in its own signature the DTO it maps.
	 */
	private Method entityMethod(Class<?> entityType) {
		return entityMethods.computeIfAbsent(entityType, type -> importMethod(
			"entity",
			m -> m.getParameterCount() == 1 && m.getReturnType() == type,
			type));
	}

	/**
	 * The cached {@code ConfigEntityMapper.update(...)} overload for the entity type,
	 * resolved by its (first) {@code @MappingTarget} parameter.
	 */
	private Method updateMethod(Class<?> entityType) {
		return updateMethods.computeIfAbsent(entityType, type -> importMethod(
			"update",
			m -> m.getParameterCount() == 2 && m.getParameterTypes()[0] == type,
			type));
	}

	/**
	 * The first {@link ConfigEntityMapper} method with the given name that matches;
	 * fails loudly when a registered exportable type has no matching overload.
	 */
	private static Method importMethod(
		String name, Predicate<Method> match, Class<?> entityType) {

		for (Method method : ConfigEntityMapper.class.getMethods()) {
			if (method.getName().equals(name) && match.test(method)) {
				return method;
			}
		}
		throw new IllegalStateException(
			"ConfigEntityMapper has no " + name + "(...) for "
				+ entityType.getSimpleName());
	}

	/**
	 * Maps the typed attributes to a transient entity through the {@code entity}
	 * overload for the entity type. That overload declares the DTO the
	 * {@code ConfigEntityType} declares, so create-only fields (e.g.
	 * {@code RAGConfiguration.type}) are mapped rather than silently narrowed.
	 */
	private K9Entity toEntity(Class<?> entityType, Object attributes) {
		try {
			return (K9Entity) entityMethod(entityType)
				.invoke(configEntityMapper, attributes);
		}
		catch (ReflectiveOperationException e) {
			throw new IllegalStateException(
				"Cannot map attributes to " + entityType.getSimpleName(), e);
		}
	}

	/**
	 * Applies the typed attributes onto an existing managed entity through the
	 * {@code update} overload for the entity type. That overload may declare the base
	 * DTO, so immutable create-only fields (e.g. {@code RAGConfiguration.type}) are
	 * simply not mapped and an overwrite never rewrites them. {@link Method#invoke}
	 * still accepts the attributes when they are a subtype of the declared DTO.
	 */
	private void updateEntity(Class<?> entityType, K9Entity target, Object attributes) {
		try {
			updateMethod(entityType).invoke(configEntityMapper, target, attributes);
		}
		catch (ReflectiveOperationException e) {
			throw new IllegalStateException(
				"Cannot update " + entityType.getSimpleName() + " from its attributes",
				e);
		}
	}

	private JsonNode parsedJsonConfig(ObjectNode tree) {
		JsonNode jsonConfig = tree.get("jsonConfig");
		if (jsonConfig == null || !jsonConfig.isTextual()) {
			return null;
		}
		return tryParse(jsonConfig.asText());
	}

	/**
	 * Rebinds the tenant's {@code TenantBinding} (row id 1) to the imported bucket
	 * and models resolved from the package metadata, leaving {@code virtualHost}
	 * untouched. Missing or unresolved pointers are left as they are.
	 */
	private Uni<Void> rebindTenantBinding(
		Mutiny.Session s, ConfigMetadata metadata, Map<String, Long> resolvedIds) {

		if (metadata == null) {
			return Uni.createFrom().voidItem();
		}

		Long bucketId = resolvedIds.get(metadata.getDefaultBucketRef());
		Long embeddingId = resolvedIds.get(metadata.getEnabledEmbeddingModelRef());
		Long llmId = resolvedIds.get(metadata.getEnabledLargeLanguageModelRef());

		if (bucketId == null && embeddingId == null && llmId == null) {
			return Uni.createFrom().voidItem();
		}

		return s.find(TenantBinding.class, 1L).flatMap(tenantBinding -> {
			if (tenantBinding == null) {
				return Uni.createFrom().voidItem();
			}
			if (bucketId != null) {
				tenantBinding.setBucket(s.getReference(Bucket.class, bucketId));
			}
			if (embeddingId != null) {
				tenantBinding.setEmbeddingModel(
					s.getReference(EmbeddingModel.class, embeddingId));
			}
			if (llmId != null) {
				tenantBinding.setLargeLanguageModel(
					s.getReference(LargeLanguageModel.class, llmId));
			}
			return s.persist(tenantBinding).call(s::flush);
		});
	}

	private Uni<PlannedAction.Action> rebuildAclMapping(
		Mutiny.Session s, ConfigEntity entity, Map<String, Long> resolvedIds) {

		Long pluginDriverId = endpoint(entity, "pluginDriver", resolvedIds);
		Long docTypeFieldId = endpoint(entity, "docTypeField", resolvedIds);
		if (pluginDriverId == null || docTypeFieldId == null) {
			return Uni.createFrom().item(PlannedAction.Action.SKIP);
		}

		var userField =
			((AclMappingRepresentation) entity.getAttributes()).getUserField();
		PluginDriverDocTypeFieldKey key =
			PluginDriverDocTypeFieldKey.of(pluginDriverId, docTypeFieldId);

		return s.find(AclMapping.class, key).flatMap(existing -> {
			if (existing != null) {
				existing.setUserField(userField);
				return s.persist(existing).call(s::flush)
					.replaceWith(PlannedAction.Action.OVERWRITE);
			}
			AclMapping mapping = new AclMapping();
			mapping.setKey(key);
			mapping.setPluginDriver(s.getReference(PluginDriver.class, pluginDriverId));
			mapping.setDocTypeField(s.getReference(DocTypeField.class, docTypeFieldId));
			mapping.setUserField(userField);
			return s.persist(mapping).call(s::flush)
				.replaceWith(PlannedAction.Action.CREATE);
		});
	}

	private Uni<PlannedAction.Action> rebuildEnrichPipelineItem(
		Mutiny.Session s, ConfigEntity entity, Map<String, Long> resolvedIds) {

		Long pipelineId = endpoint(entity, "enrichPipeline", resolvedIds);
		Long itemId = endpoint(entity, "enrichItem", resolvedIds);
		if (pipelineId == null || itemId == null) {
			return Uni.createFrom().item(PlannedAction.Action.SKIP);
		}

		Float weight =
			((EnrichPipelineItemRepresentation) entity.getAttributes()).getWeight();
		EnrichPipelineItemKey key = EnrichPipelineItemKey.of(pipelineId, itemId);

		return s.find(EnrichPipelineItem.class, key).flatMap(existing -> {
			if (existing != null) {
				existing.setWeight(weight);
				return s.persist(existing).call(s::flush)
					.replaceWith(PlannedAction.Action.OVERWRITE);
			}
			EnrichPipelineItem item = new EnrichPipelineItem();
			item.setKey(key);
			item.setEnrichPipeline(s.getReference(EnrichPipeline.class, pipelineId));
			item.setEnrichItem(s.getReference(EnrichItem.class, itemId));
			item.setWeight(weight);
			return s.persist(item).call(s::flush)
				.replaceWith(PlannedAction.Action.CREATE);
		});
	}

	/**
	 * Rebuilds the composite-key join entities from their resolved endpoints. They
	 * carry no plan action; the exporter emits them with dedicated builders, so the
	 * importer mirrors that with a dedicated rebuild per join type. Each rebuild
	 * reports its outcome so the joins are reflected in the import counts, which
	 * would otherwise total fewer than the package: the join entities were applied
	 * but invisible in the summary.
	 */
	private Uni<JoinCounts> rebuildJoins(
		Mutiny.Session s, ConfigPackage pkg, Map<String, Long> resolvedIds) {

		Uni<JoinCounts> chain = Uni.createFrom().item(new JoinCounts(0, 0, 0));
		for (ConfigEntity entity : pkg.getEntities()) {
			if (!ConfigMatcher.isJoinEntity(entity.getType().getEntityType())) {
				continue;
			}
			chain = chain.flatMap(counts -> (switch (entity.getType()) {
				case ENRICH_PIPELINE_ITEM ->
					rebuildEnrichPipelineItem(s, entity, resolvedIds);
				case ACL_MAPPING ->
					rebuildAclMapping(s, entity, resolvedIds);
				default -> throw new IllegalStateException(
					"not a join type: " + entity.getType());
			}).map(counts::add));
		}
		return chain;
	}

	/**
	 * Tally of the join entities rebuilt by {@link #rebuildJoins}, classified with
	 * the same three outcomes as planned actions so they fold into the import counts.
	 */
	private record JoinCounts(int created, int overwritten, int skipped) {

		JoinCounts add(PlannedAction.Action outcome) {
			return switch (outcome) {
				case CREATE -> new JoinCounts(created + 1, overwritten, skipped);
				case OVERWRITE -> new JoinCounts(created, overwritten + 1, skipped);
				case SKIP -> new JoinCounts(created, overwritten, skipped + 1);
			};
		}
	}

	private List<Object> resolveReferences(
		Mutiny.Session s, K9Entity entity, String relationship,
		Class<?> elementType, List<String> handles, Map<String, Long> resolvedIds) {

		List<Object> targets = new ArrayList<>();
		if (handles != null) {
			for (String handle : handles) {
				Long id = resolvedIds.get(handle);
				if (id != null) {
					targets.add(s.getReference(elementType, id));
				}
				else {
					warnUnresolved(entity, relationship, handle);
				}
			}
		}
		return targets;
	}

	/**
	 * Restores the target's current secret values into the incoming attributes so
	 * an overwrite keeps existing secrets: top-level fields are copied from the
	 * target entity, and placeholders nested in {@code jsonConfig} are replaced with
	 * the target's values at the same position.
	 */
	private Object restoreRedacted(
		Object attributes, List<String> redactedFields, K9Entity target) {

		if (redactedFields == null || redactedFields.isEmpty()) {
			return attributes;
		}

		ObjectNode tree = objectMapper.valueToTree(attributes);
		boolean jsonConfigRedacted = false;
		for (String path : redactedFields) {
			if (path.startsWith("jsonConfig.")) {
				jsonConfigRedacted = true;
			}
			else {
				Object value = readProperty(target, path);
				if (value == null) {
					tree.remove(path);
				}
				else {
					tree.set(path, objectMapper.valueToTree(value));
				}
			}
		}
		if (jsonConfigRedacted) {
			JsonNode config = parsedJsonConfig(tree);
			if (config != null) {
				Object targetConfig = readProperty(target, "jsonConfig");
				JsonNode targetTree = targetConfig == null
					? null
					: tryParse(targetConfig.toString());
				restorePlaceholders(config, targetTree);
				tree.put("jsonConfig", writeString(config));
			}
		}
		return bindBack(tree, attributes.getClass());
	}

	/**
	 * On create the entity is transient, so a fresh collection is set directly; on
	 * overwrite the managed collection is fetched, then replaced in place so
	 * Hibernate can compute the difference at flush.
	 */
	@SuppressWarnings("unchecked")
	private Uni<Void> setCollection(
		Mutiny.Session s, K9Entity entity, Field field, List<Object> targets,
		boolean isNew) {

		if (isNew) {
			setField(entity, field, newCollection(field, targets));
			return Uni.createFrom().voidItem();
		}

		Object current = getField(entity, field);
		if (current == null) {
			setField(entity, field, newCollection(field, targets));
			return Uni.createFrom().voidItem();
		}
		return s.fetch(current).invoke(fetched -> {
			Collection<Object> collection = (Collection<Object>) fetched;
			collection.clear();
			collection.addAll(targets);
		}).replaceWithVoid();
	}

	/**
	 * Removes the redacted values from the attributes so the placeholder is never
	 * persisted on create: top-level fields are dropped and secrets nested in
	 * {@code jsonConfig} are stripped from the parsed config.
	 */
	private Object stripRedacted(Object attributes, List<String> redactedFields) {
		if (redactedFields == null || redactedFields.isEmpty()) {
			return attributes;
		}

		ObjectNode tree = objectMapper.valueToTree(attributes);
		boolean jsonConfigRedacted = false;
		for (String path : redactedFields) {
			if (path.startsWith("jsonConfig.")) {
				jsonConfigRedacted = true;
			}
			else {
				tree.remove(path);
			}
		}
		if (jsonConfigRedacted) {
			JsonNode config = parsedJsonConfig(tree);
			if (config != null) {
				stripPlaceholders(config);
				tree.put("jsonConfig", writeString(config));
			}
		}
		return bindBack(tree, attributes.getClass());
	}

	private JsonNode tryParse(String json) {
		try {
			return objectMapper.readTree(json);
		}
		catch (JsonProcessingException e) {
			return null;
		}
	}

	/**
	 * (Re)wires every owning association of the entity from the package references
	 * to the real target ids resolved so far. To-one fields are set to a reference
	 * proxy; to-many collections are replaced with the resolved targets (fetched
	 * first when overwriting a managed collection). Handles that resolve to nothing
	 * (targets outside the package) are skipped.
	 */
	private Uni<Void> wireAssociations(
		Mutiny.Session s, K9Entity entity, Map<String, List<String>> references,
		Map<String, Long> resolvedIds, boolean isNew) {

		if (references == null || references.isEmpty()) {
			return Uni.createFrom().voidItem();
		}

		Uni<Void> chain = Uni.createFrom().voidItem();
		for (Map.Entry<String, List<String>> reference : references.entrySet()) {
			Field field = findField(entity.getClass(), reference.getKey());
			if (field == null) {
				continue;
			}
			List<String> handles = reference.getValue();

			if (Collection.class.isAssignableFrom(field.getType())) {
				List<Object> targets = resolveReferences(
					s, entity, reference.getKey(), elementType(field),
					handles, resolvedIds);
				chain = chain.flatMap(ignore ->
					setCollection(s, entity, field, targets, isNew));
			}
			else if (handles != null && !handles.isEmpty()) {
				String handle = handles.get(0);
				Long id = resolvedIds.get(handle);
				if (id != null) {
					Class<?> targetType = field.getType();
					// Load, don't just reference: a to-one may be read by a lifecycle
					// callback at flush (e.g. DocTypeField.refreshPath reads
					// docType.getName()), and a reactive session cannot lazily fetch a
					// bare reference proxy.
					chain = chain.flatMap(ignore -> s.find(targetType, id)
						.invoke(target -> setField(entity, field, target))
						.replaceWithVoid());
				}
				else {
					warnUnresolved(entity, reference.getKey(), handle);
				}
			}
		}
		return chain;
	}

	private String writeString(JsonNode node) {
		try {
			return objectMapper.writeValueAsString(node);
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException("Unable to re-serialize jsonConfig", e);
		}
	}

	private static Class<?> elementType(Field field) {
		Type generic = field.getGenericType();
		if (generic instanceof ParameterizedType parameterized) {
			Type[] arguments = parameterized.getActualTypeArguments();
			if (arguments.length == 1 && arguments[0] instanceof Class<?> element) {
				return element;
			}
		}
		throw new IllegalStateException(
			"Cannot resolve element type of collection field " + field);
	}

	private static Long endpoint(
		ConfigEntity entity, String relationship, Map<String, Long> resolvedIds) {

		Map<String, List<String>> references = entity.getReferences();
		if (references == null) {
			return null;
		}
		List<String> handles = references.get(relationship);
		if (handles == null || handles.isEmpty()) {
			return null;
		}
		return resolvedIds.get(handles.get(0));
	}

	private static Field findField(Class<?> type, String name) {
		for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
			try {
				return c.getDeclaredField(name);
			}
			catch (NoSuchFieldException ignored) {
				// try the superclass
			}
		}
		return null;
	}

	private static Object getField(Object target, Field field) {
		try {
			field.setAccessible(true);
			return field.get(target);
		}
		catch (IllegalAccessException e) {
			throw new IllegalStateException("Cannot read field " + field, e);
		}
	}

	private static boolean isPlaceholder(JsonNode node) {
		return node != null && node.isTextual()
			&& ConfigRedactor.PLACEHOLDER.equals(node.asText());
	}

	private static Collection<Object> newCollection(Field field, List<Object> targets) {
		Collection<Object> collection = Set.class.isAssignableFrom(field.getType())
			? new LinkedHashSet<>()
			: new ArrayList<>();
		collection.addAll(targets);
		return collection;
	}

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

	private static void restorePlaceholders(JsonNode source, JsonNode target) {
		if (source.isObject()) {
			ObjectNode object = (ObjectNode) source;
			List<String> names = new ArrayList<>();
			object.fieldNames().forEachRemaining(names::add);
			for (String name : names) {
				JsonNode value = object.get(name);
				JsonNode targetValue =
					target != null && target.isObject() ? target.get(name) : null;
				if (isPlaceholder(value)) {
					if (targetValue != null && !targetValue.isNull()) {
						object.set(name, targetValue);
					}
					else {
						object.remove(name);
					}
				}
				else {
					restorePlaceholders(value, targetValue);
				}
			}
		}
		else if (source.isArray()) {
			ArrayNode array = (ArrayNode) source;
			for (int i = 0; i < array.size(); i++) {
				JsonNode targetValue = target != null && target.isArray()
					&& i < target.size() ? target.get(i) : null;
				restorePlaceholders(array.get(i), targetValue);
			}
		}
	}

	private static void setField(Object target, Field field, Object value) {
		try {
			field.setAccessible(true);
			field.set(target, value);
		}
		catch (IllegalAccessException e) {
			throw new IllegalStateException("Cannot set field " + field, e);
		}
	}

	private static void stripPlaceholders(JsonNode node) {
		if (node.isObject()) {
			ObjectNode object = (ObjectNode) node;
			List<String> names = new ArrayList<>();
			object.fieldNames().forEachRemaining(names::add);
			for (String name : names) {
				JsonNode value = object.get(name);
				if (isPlaceholder(value)) {
					object.remove(name);
				}
				else {
					stripPlaceholders(value);
				}
			}
		}
		else if (node.isArray()) {
			for (JsonNode element : node) {
				stripPlaceholders(element);
			}
		}
	}

	private static void warnUnresolved(
		K9Entity entity, String relationship, String handle) {

		log.warnf(
			"Import: reference '%s' -> '%s' on %s could not be resolved "
				+ "(target not in package); skipping",
			relationship, handle, entity.getClass().getSimpleName());
	}

}
