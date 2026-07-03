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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.openk9.datasource.config.model.ConfigEntity;
import io.openk9.datasource.config.model.ConfigMetadata;
import io.openk9.datasource.config.model.ConfigPackage;
import io.openk9.datasource.config.model.ImportMode;
import io.openk9.datasource.config.model.ImportPlan;
import io.openk9.datasource.config.model.ImportResult;
import io.openk9.datasource.config.model.PlannedAction;
import io.openk9.datasource.config.model.representation.AclMappingRepresentation;
import io.openk9.datasource.config.model.representation.EnrichPipelineItemRepresentation;
import io.openk9.datasource.mapper.K9EntityMapper;
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
import io.openk9.datasource.model.dto.base.K9EntityDTO;
import io.openk9.datasource.model.util.K9Entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

/**
 * Applies an {@link ImportPlan} to a target tenant, in a single transaction.
 * <p>
 * The plan (from {@link ConfigMatcher}) is matched and applied on the same
 * session, so nothing changes between planning and writing. Regular entities are
 * created or overwritten generically: scalars come from the typed attributes DTO
 * through the existing per-type {@link K9EntityMapper}, and every association is
 * (re)wired from the package {@code references} to the real target ids resolved
 * along the way. The two composite-key join entities ({@code EnrichPipelineItem},
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
	private final ObjectMapper objectMapper;

	// entity class -> its DTO mapper, resolved once from the CDI container.
	private final Map<Class<?>, K9EntityMapper<K9Entity, K9EntityDTO>> mappers =
		new HashMap<>();

	@Inject
	public ConfigImporter(
		Mutiny.SessionFactory sessionFactory,
		ConfigMatcher matcher,
		@Any Instance<K9EntityMapper<?, ?>> mapperBeans,
		ObjectMapper objectMapper) {

		this.sessionFactory = sessionFactory;
		this.matcher = matcher;
		this.objectMapper = objectMapper;
		registerMappers(mapperBeans);
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
			.flatMap(ignore -> rebindTenantBinding(s, pkg.getMetadata(), resolvedIds))
			.map(ignore -> new ImportResult(
				(int) plan.count(PlannedAction.Action.CREATE),
				(int) plan.count(PlannedAction.Action.OVERWRITE),
				(int) plan.count(PlannedAction.Action.SKIP),
				resolvedIds));
	}

	private Uni<Void> applyAction(
		Mutiny.Session s, ConfigEntity entity, PlannedAction action,
		Map<String, Long> resolvedIds) {

		Class<?> entityClass = action.type().getEntityType();
		String ref = action.ref();

		switch (action.action()) {
			case SKIP -> {
				resolvedIds.put(ref, action.existingId());
				return Uni.createFrom().voidItem();
			}
			case CREATE -> {
				Object dto = stripRedacted(
					entity.getAttributes(), entity.getRedactedFields());
				K9Entity created = mapperFor(entityClass).create((K9EntityDTO) dto);
				return wireAssociations(
						s, created, entity.getReferences(), resolvedIds, true)
					.flatMap(ignore -> s.persist(created).call(s::flush))
					.invoke(() -> resolvedIds.put(ref, created.getId()));
			}
			case OVERWRITE -> {
				return s.find(entityClass, action.existingId()).flatMap(found -> {
					K9Entity target = (K9Entity) found;
					Object dto = restoreRedacted(
						entity.getAttributes(), entity.getRedactedFields(), target);
					mapperFor(entityClass).update(target, (K9EntityDTO) dto);
					return wireAssociations(
							s, target, entity.getReferences(), resolvedIds, false)
						.flatMap(ignore -> s.persist(target).call(s::flush))
						.invoke(() -> resolvedIds.put(ref, action.existingId()));
				});
			}
			default -> {
				return Uni.createFrom().voidItem();
			}
		}
	}

	// --- association rewiring --------------------------------------------------

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
					setField(entity, field, s.getReference(field.getType(), id));
				}
				else {
					warnUnresolved(entity, reference.getKey(), handle);
				}
			}
		}
		return chain;
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

	private static void warnUnresolved(
		K9Entity entity, String relationship, String handle) {

		log.warnf(
			"Import: reference '%s' -> '%s' on %s could not be resolved "
				+ "(target not in package); skipping",
			relationship, handle, entity.getClass().getSimpleName());
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

	private static Collection<Object> newCollection(Field field, List<Object> targets) {
		Collection<Object> collection = Set.class.isAssignableFrom(field.getType())
			? new LinkedHashSet<>()
			: new ArrayList<>();
		collection.addAll(targets);
		return collection;
	}

	// --- join entities ---------------------------------------------------------

	/**
	 * Rebuilds the composite-key join entities from their resolved endpoints. They
	 * carry no plan action; the exporter emits them with dedicated builders, so the
	 * importer mirrors that with a dedicated rebuild per join type.
	 */
	private Uni<Void> rebuildJoins(
		Mutiny.Session s, ConfigPackage pkg, Map<String, Long> resolvedIds) {

		Uni<Void> chain = Uni.createFrom().voidItem();
		for (ConfigEntity entity : pkg.getEntities()) {
			if (!ConfigMatcher.isJoinEntity(entity.getType().getEntityType())) {
				continue;
			}
			chain = chain.flatMap(ignore -> switch (entity.getType()) {
				case ENRICH_PIPELINE_ITEM ->
					rebuildEnrichPipelineItem(s, entity, resolvedIds);
				case ACL_MAPPING ->
					rebuildAclMapping(s, entity, resolvedIds);
				default -> Uni.createFrom().voidItem();
			});
		}
		return chain;
	}

	private Uni<Void> rebuildEnrichPipelineItem(
		Mutiny.Session s, ConfigEntity entity, Map<String, Long> resolvedIds) {

		Long pipelineId = endpoint(entity, "enrichPipeline", resolvedIds);
		Long itemId = endpoint(entity, "enrichItem", resolvedIds);
		if (pipelineId == null || itemId == null) {
			return Uni.createFrom().voidItem();
		}

		Float weight =
			((EnrichPipelineItemRepresentation) entity.getAttributes()).getWeight();
		EnrichPipelineItemKey key = EnrichPipelineItemKey.of(pipelineId, itemId);

		return s.find(EnrichPipelineItem.class, key).flatMap(existing -> {
			if (existing != null) {
				existing.setWeight(weight);
				return s.persist(existing).call(s::flush);
			}
			EnrichPipelineItem item = new EnrichPipelineItem();
			item.setKey(key);
			item.setEnrichPipeline(s.getReference(EnrichPipeline.class, pipelineId));
			item.setEnrichItem(s.getReference(EnrichItem.class, itemId));
			item.setWeight(weight);
			return s.persist(item).call(s::flush);
		});
	}

	private Uni<Void> rebuildAclMapping(
		Mutiny.Session s, ConfigEntity entity, Map<String, Long> resolvedIds) {

		Long pluginDriverId = endpoint(entity, "pluginDriver", resolvedIds);
		Long docTypeFieldId = endpoint(entity, "docTypeField", resolvedIds);
		if (pluginDriverId == null || docTypeFieldId == null) {
			return Uni.createFrom().voidItem();
		}

		var userField =
			((AclMappingRepresentation) entity.getAttributes()).getUserField();
		PluginDriverDocTypeFieldKey key =
			PluginDriverDocTypeFieldKey.of(pluginDriverId, docTypeFieldId);

		return s.find(AclMapping.class, key).flatMap(existing -> {
			if (existing != null) {
				existing.setUserField(userField);
				return s.persist(existing).call(s::flush);
			}
			AclMapping mapping = new AclMapping();
			mapping.setKey(key);
			mapping.setPluginDriver(s.getReference(PluginDriver.class, pluginDriverId));
			mapping.setDocTypeField(s.getReference(DocTypeField.class, docTypeFieldId));
			mapping.setUserField(userField);
			return s.persist(mapping).call(s::flush);
		});
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

	// --- tenant binding rebind -------------------------------------------------

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

	// --- secrets ---------------------------------------------------------------

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

	private JsonNode parsedJsonConfig(ObjectNode tree) {
		JsonNode jsonConfig = tree.get("jsonConfig");
		if (jsonConfig == null || !jsonConfig.isTextual()) {
			return null;
		}
		return tryParse(jsonConfig.asText());
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

	private static boolean isPlaceholder(JsonNode node) {
		return node != null && node.isTextual()
			&& ConfigRedactor.PLACEHOLDER.equals(node.asText());
	}

	private JsonNode tryParse(String json) {
		try {
			return objectMapper.readTree(json);
		}
		catch (JsonProcessingException e) {
			return null;
		}
	}

	private String writeString(JsonNode node) {
		try {
			return objectMapper.writeValueAsString(node);
		}
		catch (JsonProcessingException e) {
			throw new IllegalStateException("Unable to re-serialize jsonConfig", e);
		}
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

	// --- mapper registry -------------------------------------------------------

	@SuppressWarnings("unchecked")
	private void registerMappers(Instance<K9EntityMapper<?, ?>> mapperBeans) {
		for (K9EntityMapper<?, ?> mapper : mapperBeans) {
			Class<?> entityClass = resolveEntityType(mapper.getClass());
			if (entityClass != null) {
				mappers.putIfAbsent(
					entityClass, (K9EntityMapper<K9Entity, K9EntityDTO>) mapper);
			}
		}
	}

	private K9EntityMapper<K9Entity, K9EntityDTO> mapperFor(Class<?> entityClass) {
		K9EntityMapper<K9Entity, K9EntityDTO> mapper = mappers.get(entityClass);
		if (mapper == null) {
			throw new IllegalStateException(
				"No K9EntityMapper for " + entityClass.getName());
		}
		return mapper;
	}

	/**
	 * The {@code ENTITY} type argument of the {@link K9EntityMapper} the bean
	 * implements, found by walking its (generic) interface hierarchy.
	 */
	private static Class<?> resolveEntityType(Class<?> mapperImpl) {
		List<ParameterizedType> parameterized = new ArrayList<>();
		collectParameterizedInterfaces(mapperImpl, parameterized, new HashSet<>());
		for (ParameterizedType type : parameterized) {
			if (type.getRawType() == K9EntityMapper.class) {
				Type argument = type.getActualTypeArguments()[0];
				if (argument instanceof Class<?> entityClass) {
					return entityClass;
				}
			}
		}
		return null;
	}

	private static void collectParameterizedInterfaces(
		Class<?> type, List<ParameterizedType> out, Set<Class<?>> seen) {

		if (type == null || type == Object.class || !seen.add(type)) {
			return;
		}
		for (Type genericInterface : type.getGenericInterfaces()) {
			if (genericInterface instanceof ParameterizedType parameterized) {
				out.add(parameterized);
				collectParameterizedInterfaces(
					(Class<?>) parameterized.getRawType(), out, seen);
			}
			else if (genericInterface instanceof Class<?> rawInterface) {
				collectParameterizedInterfaces(rawInterface, out, seen);
			}
		}
		collectParameterizedInterfaces(type.getSuperclass(), out, seen);
	}

	// --- reflection helpers ----------------------------------------------------

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

	private static Object getField(Object target, Field field) {
		try {
			field.setAccessible(true);
			return field.get(target);
		}
		catch (IllegalAccessException e) {
			throw new IllegalStateException("Cannot read field " + field, e);
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

}
