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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.openk9.datasource.config.model.ConfigEntity;
import io.openk9.datasource.config.model.ConfigEntityType;
import io.openk9.datasource.config.model.ConfigPackage;
import io.openk9.datasource.config.model.ImportPlan;
import io.openk9.datasource.config.model.PlannedAction;
import io.openk9.datasource.model.DataIndex;
import io.openk9.datasource.model.Datasource;
import io.openk9.datasource.model.TenantBinding;
import io.openk9.datasource.model.dto.base.DatasourceDTO;
import io.openk9.ml.grpc.EmbeddingOuterClass;

import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

/**
 * Checks, before anything is written, that the datasources of a package will
 * be able to create the first dataIndex their configuration asks for.
 * <p>
 * A datasource that is created, or overwritten while it has no dataIndex,
 * creates its first dataIndex from its own configuration when it is first
 * scheduled. A knn index that has no field to embed, or no embedding model in
 * the tenant to give it a vector mapping, can not be created: that is reported
 * now, instead of as a failed scheduling later. A datasource that already has
 * a dataIndex keeps it, and the report says so when the package asks for a
 * different one.
 */
final class IndexIntentChecker {

	private static final String EMBEDDING_DOC_TYPE_FIELD = "embeddingDocTypeField";

	private IndexIntentChecker() {
	}

	/**
	 * What the check found.
	 *
	 * @param errors   the reasons the import can not proceed
	 * @param warnings what the operator should know, without stopping the import
	 */
	record Findings(List<String> errors, List<String> warnings) {}

	/**
	 * Checks every datasource of the package that the plan creates or overwrites.
	 *
	 * @param session the session of the import
	 * @param pkg     the package to import
	 * @param plan    the plan of the import
	 * @return the findings
	 */
	static Uni<Findings> check(
		Mutiny.Session session, ConfigPackage pkg, ImportPlan plan) {

		Map<String, PlannedAction> actions = plan.byRef();

		List<ConfigEntity> datasources = pkg.getEntities().stream()
			.filter(entity -> entity.getType() == ConfigEntityType.DATASOURCE)
			.filter(entity -> entity.getAttributes() instanceof DatasourceDTO)
			.filter(entity -> {
				PlannedAction action = actions.get(entity.getRef());
				return action != null && action.action() != PlannedAction.Action.SKIP;
			})
			.toList();

		if (datasources.isEmpty()) {
			return Uni.createFrom().item(new Findings(List.of(), List.of()));
		}

		Set<Long> existingIds = datasources.stream()
			.map(entity -> actions.get(entity.getRef()))
			.filter(action -> action.action() == PlannedAction.Action.OVERWRITE)
			.map(PlannedAction::existingId)
			.collect(Collectors.toSet());

		Uni<Map<Long, Datasource>> targets = existingIds.isEmpty()
			? Uni.createFrom().item(Map.of())
			: session
				.createQuery(
					"select d from Datasource d "
						+ "left join fetch d.dataIndex "
						+ "left join fetch d.embeddingDocTypeField "
						+ "where d.id in :ids",
					Datasource.class)
				.setParameter("ids", existingIds)
				.getResultList()
				.map(found -> found.stream().collect(
					Collectors.toMap(Datasource::getId, Function.identity())));

		return targets.flatMap(found -> session.find(TenantBinding.class, 1L)
			.map(binding -> {
				boolean modelAvailable = packageEnablesEmbeddingModel(pkg)
					|| (binding != null && binding.getEmbeddingModel() != null);

				List<String> errors = new ArrayList<>();
				List<String> warnings = new ArrayList<>();

				for (ConfigEntity entity : datasources) {
					PlannedAction action = actions.get(entity.getRef());
					Datasource target = action.existingId() == null
						? null
						: found.get(action.existingId());

					check(entity, target, modelAvailable, errors, warnings);
				}

				return new Findings(List.copyOf(errors), List.copyOf(warnings));
			}));
	}

	private static void check(
		ConfigEntity entity, Datasource target, boolean modelAvailable,
		List<String> errors, List<String> warnings) {

		DatasourceDTO dto = (DatasourceDTO) entity.getAttributes();
		String name = entity.getKey();

		if (target == null && !written(entity, "knnIndex")) {
			warnings.add(String.format(
				"Datasource '%s' comes from a package that carries no index "
					+ "configuration: its first index will be created with the "
					+ "defaults, without vectors", name));
			return;
		}

		boolean knn = written(entity, "knnIndex")
			? Boolean.TRUE.equals(dto.getKnnIndex())
			: Boolean.TRUE.equals(target.getKnnIndex());

		if (target != null && target.getDataIndex() != null) {
			List<String> differences = differences(entity, dto, target.getDataIndex(), knn);

			if (!differences.isEmpty()) {
				warnings.add(String.format(
					"Datasource '%s' already has a dataIndex, which is kept as it "
						+ "is: the index configuration of the package (%s differ) is "
						+ "saved and applies only to a dataIndex created when the "
						+ "datasource has none", name, String.join(", ", differences)));
			}
			return;
		}

		if (!knn) {
			return;
		}

		boolean embeddingField = entity.getReferences() != null
			&& entity.getReferences().containsKey(EMBEDDING_DOC_TYPE_FIELD)
			? !entity.getReferences().get(EMBEDDING_DOC_TYPE_FIELD).isEmpty()
			: target != null && target.getEmbeddingDocTypeField() != null;

		if (!embeddingField) {
			errors.add(String.format(
				"Datasource '%s' asks for a knn index but no field to embed is "
					+ "given: it needs an embeddingDocTypeField", name));
		}

		if (!modelAvailable) {
			errors.add(String.format(
				"Datasource '%s' asks for a knn index but the tenant has no "
					+ "embedding model enabled and the package does not provide "
					+ "one", name));
		}
	}

	private static List<String> differences(
		ConfigEntity entity, DatasourceDTO dto, DataIndex dataIndex, boolean knn) {

		List<String> differences = new ArrayList<>();

		if (knn != Boolean.TRUE.equals(dataIndex.getKnnIndex())) {
			differences.add("knnIndex");
		}

		if (written(entity, "chunkType")
			&& !Objects.equals(
				Objects.requireNonNullElse(
					dto.getChunkType(), EmbeddingOuterClass.ChunkType.CHUNK_TYPE_DEFAULT),
				dataIndex.getChunkType())) {

			differences.add("chunkType");
		}

		if (written(entity, "chunkWindowSize")
			&& !Objects.equals(
				Objects.requireNonNullElse(dto.getChunkWindowSize(), 0),
				dataIndex.getChunkWindowSize())) {

			differences.add("chunkWindowSize");
		}

		return differences;
	}

	private static boolean packageEnablesEmbeddingModel(ConfigPackage pkg) {
		if (pkg.getMetadata() == null
			|| pkg.getMetadata().getEnabledEmbeddingModelRef() == null) {

			return false;
		}

		String ref = pkg.getMetadata().getEnabledEmbeddingModelRef();

		return pkg.getEntities().stream()
			.anyMatch(candidate -> ref.equals(candidate.getRef()));
	}

	private static boolean written(ConfigEntity entity, String attribute) {
		return entity.getAttributeKeys() == null
			|| entity.getAttributeKeys().contains(attribute);
	}

}
