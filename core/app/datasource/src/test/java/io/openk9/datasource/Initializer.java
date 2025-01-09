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

package io.openk9.datasource;

import io.openk9.datasource.graphql.dto.DatasourceConnectionDTO;
import io.openk9.datasource.graphql.dto.PipelineWithItemsDTO;
import io.openk9.datasource.model.EnrichItem;
import io.openk9.datasource.model.dto.EmbeddingModelDTO;
import io.openk9.datasource.model.dto.EnrichItemDTO;
import io.openk9.datasource.model.dto.LargeLanguageModelDTO;
import io.openk9.datasource.model.dto.SuggestionCategoryDTO;
import io.openk9.datasource.model.dto.TabDTO;
import io.openk9.datasource.model.init.Bucket;
import io.openk9.datasource.plugindriver.WireMockPluginDriver;
import io.openk9.datasource.service.BucketService;
import io.openk9.datasource.service.CreateConnection;
import io.openk9.datasource.service.DatasourceService;
import io.openk9.datasource.service.EmbeddingModelService;
import io.openk9.datasource.service.EnrichItemService;
import io.openk9.datasource.service.LargeLanguageModelService;
import io.openk9.datasource.service.SuggestionCategoryService;
import io.openk9.datasource.service.TabService;
import io.openk9.datasource.service.TenantInitializerService;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class Initializer {

	private static final Logger log = Logger.getLogger(Initializer.class);
	@Inject
	BucketService bucketService;
	@Inject
	DatasourceService datasourceService;

	@Inject
	EmbeddingModelService embeddingModelService;
	@Inject
	EnrichItemService enrichItemService;
	@Inject
	TenantInitializerService initializerService;
	@Inject
	LargeLanguageModelService largeLanguageModelService;
	@Inject
	SuggestionCategoryService suggestionCategoryService;

	@Inject
	TabService tabService;

	public void initDb(@Observes Startup startup) {

		log.info("Init public tenant with default data.");

		var bucketId = initializerService.createDefault("public")
			.await().indefinitely();

		log.infof("New tenant initialized with id %s.", bucketId);

		createDatasourceConnection();

		createPrimaryEmbeddingModel();

		createSecondaryEmbeddingModel();

		createPrimaryLLM();

		createSecondaryLLM();

		bindDatasourceToBucket();

		addsSuggestionCategoriesToBucket();

		addsTabsToBucket();

	}

	void addsSuggestionCategoriesToBucket() {

		var category1 = suggestionCategoryService.create(SuggestionCategoryDTO.builder()
			.name("Category 1")
			.multiSelect(false)
			.priority(1.0f)
			.build()
		).await().indefinitely();

		var category2 = suggestionCategoryService.create(SuggestionCategoryDTO.builder()
			.name("Category 2")
			.multiSelect(false)
			.priority(2.0f)
			.build()
		).await().indefinitely();

		var category3 = suggestionCategoryService.create(SuggestionCategoryDTO.builder()
			.name("Category 3")
			.multiSelect(false)
			.priority(3.0f)
			.build()
		).await().indefinitely();


		var bucket = bucketService
			.findByName("public", Bucket.INSTANCE.getName())
			.await()
			.indefinitely();

		bucketService.addSuggestionCategory(bucket.getId(), category1.getId())
			.await().indefinitely();

		bucketService.addSuggestionCategory(bucket.getId(), category2.getId())
			.await().indefinitely();

		bucketService.addSuggestionCategory(bucket.getId(), category3.getId())
			.await().indefinitely();

	}

	private void addsTabsToBucket() {

		var tabOne = tabService.create(TabDTO.builder()
			.name("Tab One")
			.priority(1)
			.build()
		).await().indefinitely();

		var bucket = bucketService.findByName("public", Bucket.INSTANCE.getName())
			.await().indefinitely();

		bucketService.addTabToBucket(bucket.getId(), tabOne.getId())
			.await().indefinitely();


	}

	private void bindDatasourceToBucket() {

		var datasource = datasourceService
			.findByName("public", CreateConnection.DATASOURCE_NAME)
			.await()
			.indefinitely();

		var bucket = bucketService
			.findByName("public", Bucket.INSTANCE.getName())
			.await()
			.indefinitely();

		bucketService.addDatasource(bucket.getId(), datasource.getId());

	}

	private void createDatasourceConnection() {

		var enrich1 = enrichItemService.create(EnrichItemDTO.builder()
			.name("Http Async Enrich 1")
			.type(EnrichItem.EnrichItemType.HTTP_ASYNC)
			.serviceName("http-service-1")
			.jsonPath("$")
			.jsonConfig("{}")
			.requestTimeout(60000L)
			.behaviorMergeType(EnrichItem.BehaviorMergeType.MERGE)
			.behaviorOnError(EnrichItem.BehaviorOnError.FAIL)
			.build()
		).await().indefinitely();

		var enrich2 = enrichItemService.create(EnrichItemDTO.builder()
			.name("Http Sync Enrich 2")
			.type(EnrichItem.EnrichItemType.HTTP_SYNC)
			.serviceName("http-service-2")
			.jsonPath("$")
			.jsonConfig("{}")
			.requestTimeout(60000L)
			.behaviorMergeType(EnrichItem.BehaviorMergeType.MERGE)
			.behaviorOnError(EnrichItem.BehaviorOnError.FAIL)
			.build()
		).await().indefinitely();

		var enrich3 = enrichItemService.create(EnrichItemDTO.builder()
			.name("Groovy Enrich 1")
			.type(EnrichItem.EnrichItemType.GROOVY_SCRIPT)
			.serviceName("ignore")
			.jsonPath("$")
			.jsonConfig("{}")
			.requestTimeout(60000L)
			.behaviorMergeType(EnrichItem.BehaviorMergeType.MERGE)
			.behaviorOnError(EnrichItem.BehaviorOnError.FAIL)
			.build()
		).await().indefinitely();

		log.info("Create a new Connection.");

		datasourceService.createDatasourceConnection(
			DatasourceConnectionDTO.builder()
				.name(CreateConnection.DATASOURCE_NAME)
				.description(CreateConnection.DATASOURCE_DESCRIPTION)
				.jsonConfig(CreateConnection.DATASOURCE_JSON_CONFIG)
				.scheduling(CreateConnection.SCHEDULING)
				.reindexing(CreateConnection.REINDEXING)
				.reindexable(false)
				.pluginDriver(CreateConnection.PLUGIN_DRIVER_DTO
					.toBuilder()
					.jsonConfig(JsonObject.of(
						"host", WireMockPluginDriver.HOST,
						"port", WireMockPluginDriver.PORT,
						"secure", false
					).encode())
					.build()
				)
				.pipeline(PipelineWithItemsDTO.builder()
					.name(CreateConnection.PIPELINE_NAME)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(enrich1.getId())
						.weight(1)
						.build()
					)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(enrich2.getId())
						.weight(2)
						.build()
					)
					.item(PipelineWithItemsDTO.ItemDTO.builder()
						.enrichItemId(enrich3.getId())
						.weight(3)
						.build()
					)
					.build()
				)
				.build())
			.await()
			.indefinitely();

		datasourceService.createDatasourceConnection(
				DatasourceConnectionDTO.builder()
					.name("createDatasourceConnection1")
					.description(CreateConnection.DATASOURCE_DESCRIPTION)
					.jsonConfig(CreateConnection.DATASOURCE_JSON_CONFIG)
					.scheduling(CreateConnection.SCHEDULING)
					.reindexing(CreateConnection.REINDEXING)
					.reindexable(false)
					.pluginDriver(CreateConnection.PLUGIN_DRIVER_DTO
						.toBuilder()
						.name("pluginDriverConnection1")
						.jsonConfig(JsonObject.of(
							"host", WireMockPluginDriver.HOST,
							"port", WireMockPluginDriver.PORT,
							"secure", false
						).encode())
						.build()
					)
					.build())
			.await()
			.indefinitely();

		datasourceService.createDatasourceConnection(
				DatasourceConnectionDTO.builder()
					.name("createDatasourceConnection2")
					.description(CreateConnection.DATASOURCE_DESCRIPTION)
					.jsonConfig(CreateConnection.DATASOURCE_JSON_CONFIG)
					.scheduling(CreateConnection.SCHEDULING)
					.reindexing(CreateConnection.REINDEXING)
					.reindexable(false)
					.pluginDriver(CreateConnection.PLUGIN_DRIVER_DTO
						.toBuilder()
						.name("pluginDriverConnection2")
						.jsonConfig(JsonObject.of(
							"host", WireMockPluginDriver.HOST,
							"port", WireMockPluginDriver.PORT,
							"secure", false
						).encode())
						.build()
					)
					.build())
			.await()
			.indefinitely();
	}

	private void createPrimaryEmbeddingModel() {

		log.info("Create primary EmbeddingModel.");

		var testEmbeddingModel = embeddingModelService.create(EmbeddingModelDTO.builder()
				.name("Test embedding model")
				.apiUrl("embedding-model.local")
				.apiKey("secret-key")
				.build())
			.await()
			.indefinitely();

		log.info("Enable primary Embedding Model");

		embeddingModelService.enable(testEmbeddingModel.getId())
			.await()
			.indefinitely();

	}

	private void createPrimaryLLM() {

		log.info("Create primary LLM.");

		var testLLM = largeLanguageModelService.create(LargeLanguageModelDTO.builder()
				.name("Test LLM")
				.apiUrl("llm.local")
				.apiKey("secret-key")
				.jsonConfig("{}")
				.build())
			.await()
			.indefinitely();

		log.info("Enable primary LLM.");

		largeLanguageModelService.enable(testLLM.getId())
			.await()
			.indefinitely();
	}

	private void createSecondaryEmbeddingModel() {

		log.info("Create secondary EmbeddingModel");

		embeddingModelService.create(EmbeddingModelDTO.builder()
				.name("Test embedding model disabled")
				.apiUrl("embedding-model.disabled.local")
				.apiKey("secret")
				.build())
			.await()
			.indefinitely();

	}

	private void createSecondaryLLM() {

		log.info("Create secondary LLM");

		largeLanguageModelService.create(LargeLanguageModelDTO.builder()
				.name("Test LLM Disabled")
				.apiUrl("llm.disabled.local")
				.apiKey("secret")
				.jsonConfig("{}")
				.build())
			.await()
			.indefinitely();

	}

}
